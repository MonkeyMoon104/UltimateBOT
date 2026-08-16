package com.monkey.ultimatebot.compat;

import com.monkey.ultimatebot.compat.BlockPassableAccess;
import java.lang.reflect.Method;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

/**
 * Block ray traces across Paper revisions that share Craft {@code v1_13_R2}.
 *
 * <p>{@code FluidCollisionMode}, {@code RayTraceResult}, and {@code World#rayTraceBlocks} exist from
 * 1.13.2. Paper 1.13.1 lacks all three — linking them in core crashes with {@link
 * NoClassDefFoundError}. Prefer the modern API reflectively; otherwise walk the ray in small steps.
 */
public final class RayTraceAccess {

    private static final double STEP = 0.25D;
    private static final boolean MODERN = resolveModern();
    private static final @Nullable Object FLUID_NEVER;
    private static final @Nullable Method RAY_TRACE;
    private static final @Nullable Method GET_HIT_BLOCK;

    static {
        if (MODERN) {
            FLUID_NEVER = loadFluidNever();
            RAY_TRACE = loadRayTrace();
            GET_HIT_BLOCK = loadGetHitBlock();
        } else {
            FLUID_NEVER = null;
            RAY_TRACE = null;
            GET_HIT_BLOCK = null;
        }
    }

    private RayTraceAccess() {}

    /**
     * First non-passable block along the ray, or {@code null} if the path is clear up to {@code
     * maxDistance}.
     */
    public static @Nullable Block firstHitBlock(
            World world, Location start, Vector direction, double maxDistance) {
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(direction, "direction");
        if (maxDistance < 1.0E-6D || direction.lengthSquared() < 1.0E-12D) {
            return null;
        }
        if (MODERN && RAY_TRACE != null && FLUID_NEVER != null && GET_HIT_BLOCK != null) {
            try {
                Object result =
                        RAY_TRACE.invoke(
                                world,
                                start,
                                direction.clone().normalize(),
                                Double.valueOf(maxDistance),
                                FLUID_NEVER,
                                Boolean.TRUE);
                if (result == null) {
                    return null;
                }
                Object hit = GET_HIT_BLOCK.invoke(result);
                return hit instanceof Block ? (Block) hit : null;
            } catch (ReflectiveOperationException | LinkageError ignored) {
                // fall through to traversal
            }
        }
        return traverseHit(world, start, direction, maxDistance);
    }

    /** True when no solid block is between {@code start} and {@code maxDistance} along {@code direction}. */
    public static boolean clearPath(World world, Location start, Vector direction, double maxDistance) {
        return firstHitBlock(world, start, direction, maxDistance) == null;
    }

    /** True when the path is clear, or the first hit is {@code allowed}. */
    public static boolean clearOrHits(
            World world, Location start, Vector direction, double maxDistance, Block allowed) {
        Objects.requireNonNull(allowed, "allowed");
        Block hit = firstHitBlock(world, start, direction, maxDistance);
        return hit == null || blocksEqual(hit, allowed);
    }

    private static @Nullable Block traverseHit(
            World world, Location start, Vector direction, double maxDistance) {
        Vector step = direction.clone().normalize().multiply(STEP);
        double traveled = 0.0D;
        int lastX = Integer.MIN_VALUE;
        int lastY = Integer.MIN_VALUE;
        int lastZ = Integer.MIN_VALUE;
        Location cursor = start.clone();
        while (traveled < maxDistance) {
            cursor.add(step);
            traveled += STEP;
            int x = cursor.getBlockX();
            int y = cursor.getBlockY();
            int z = cursor.getBlockZ();
            if (x == lastX && y == lastY && z == lastZ) {
                continue;
            }
            lastX = x;
            lastY = y;
            lastZ = z;
            Block block = world.getBlockAt(x, y, z);
            if (blocksRay(block)) {
                return block;
            }
        }
        return null;
    }

    private static boolean blocksRay(Block block) {
        Material type = block.getType();
        if (MaterialAirAccess.isAir(type)) {
            return false;
        }
        return !BlockPassableAccess.isPassable(block);
    }

    private static boolean blocksEqual(Block a, Block b) {
        return a.getX() == b.getX() && a.getY() == b.getY() && a.getZ() == b.getZ();
    }

    private static boolean resolveModern() {
        try {
            Class.forName("org.bukkit.FluidCollisionMode");
            Class.forName("org.bukkit.util.RayTraceResult");
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    private static @Nullable Object loadFluidNever() {
        try {
            Class<?> mode = Class.forName("org.bukkit.FluidCollisionMode");
            return mode.getField("NEVER").get(null);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static @Nullable Method loadRayTrace() {
        try {
            Class<?> mode = Class.forName("org.bukkit.FluidCollisionMode");
            return World.class.getMethod(
                    "rayTraceBlocks", Location.class, Vector.class, double.class, mode, boolean.class);
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
            return null;
        }
    }

    private static @Nullable Method loadGetHitBlock() {
        try {
            return Class.forName("org.bukkit.util.RayTraceResult").getMethod("getHitBlock");
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
            return null;
        }
    }
}
