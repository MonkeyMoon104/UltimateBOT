package com.monkey.ultimatebot.compat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Predicate;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.jspecify.annotations.Nullable;

/**
 * Cross-version {@link World} accessors.
 *
 * <p>On modern Paper, {@code World} extends {@code org.bukkit.generator.WorldInfo} and methods such
 * as {@code getName}/{@code getMinHeight}/{@code getMaxHeight} are declared there. Compiling
 * against that API embeds {@code WorldInfo} in the constant pool; loading those call sites on Paper
 * 1.17 (no {@code WorldInfo}) throws {@link NoClassDefFoundError}. Always go through this helper
 * for those members — never hard-link them from shared code.
 *
 * <p>{@link #nearbyEntities} dual-paths both {@code getNearbyEntities} overloads: the Location box
 * method is missing on Spigot 1.8 R1, and the Predicate overload is missing on Paper 1.13.1 (same
 * Craft {@code v1_13_R2} as 1.13.2). Never hard-link either.
 */
public final class WorldAccess {

    private static final @Nullable Method GET_NAME = resolve("getName");
    private static final @Nullable Method GET_UID = resolve("getUID");
    private static final @Nullable Method GET_MIN_HEIGHT = resolve("getMinHeight");
    private static final @Nullable Method GET_MAX_HEIGHT = resolve("getMaxHeight");
    private static final @Nullable Method NEARBY = resolveNearby();
    private static final @Nullable Method NEARBY_WITH_PREDICATE = resolveNearbyWithPredicate();

    private WorldAccess() {}

    public static String name(World world) {
        Objects.requireNonNull(world, "world");
        Object value = invoke(GET_NAME, world);
        if (value instanceof String) {
            String name = (String) value;
            if (!name.isEmpty()) {
                return name;
            }
        }
        Object uid = invoke(GET_UID, world);
        return uid != null ? uid.toString() : "world";
    }

    public static int minHeight(World world) {
        Objects.requireNonNull(world, "world");
        Object value = invoke(GET_MIN_HEIGHT, world);
        if (value instanceof Integer) {
            return (Integer) value;
        }
        return 0;
    }

    public static int maxHeight(World world) {
        Objects.requireNonNull(world, "world");
        Object value = invoke(GET_MAX_HEIGHT, world);
        if (value instanceof Integer) {
            return (Integer) value;
        }
        return 256;
    }

    /**
     * Nearby entities in a box. Prefer the Predicate overload when present; otherwise filter the
     * unfiltered {@code getNearbyEntities} result (Paper ≤1.13.1).
     */
    @SuppressWarnings("unchecked")
    public static Collection<Entity> nearbyEntities(
            World world,
            Location center,
            double x,
            double y,
            double z,
            @Nullable Predicate<? super Entity> predicate) {
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(center, "center");
        if (NEARBY_WITH_PREDICATE != null) {
            try {
                Object result =
                        NEARBY_WITH_PREDICATE.invoke(
                                world,
                                center,
                                Double.valueOf(x),
                                Double.valueOf(y),
                                Double.valueOf(z),
                                predicate);
                if (result instanceof Collection) {
                    return (Collection<Entity>) result;
                }
            } catch (ReflectiveOperationException | LinkageError ignored) {
                // fall through
            }
        }
        Collection<Entity> nearby = nearbyUnfiltered(world, center, x, y, z);
        if (predicate == null) {
            return nearby;
        }
        Collection<Entity> filtered = new ArrayList<>();
        for (Entity entity : nearby) {
            if (predicate.test(entity)) {
                filtered.add(entity);
            }
        }
        return filtered;
    }

    @SuppressWarnings("unchecked")
    private static Collection<Entity> nearbyUnfiltered(World world, Location center, double x, double y, double z) {
        if (NEARBY != null) {
            try {
                Object result =
                        NEARBY.invoke(
                                world,
                                center,
                                Double.valueOf(x),
                                Double.valueOf(y),
                                Double.valueOf(z));
                if (result instanceof Collection) {
                    return (Collection<Entity>) result;
                }
            } catch (ReflectiveOperationException | LinkageError ignored) {
                // Spigot 1.8 R1: method missing — scan loaded chunks.
            }
        }
        return nearbyByChunks(world, center, x, y, z);
    }

    private static Collection<Entity> nearbyByChunks(
            World world, Location center, double x, double y, double z) {
        double minX = center.getX() - x;
        double maxX = center.getX() + x;
        double minY = center.getY() - y;
        double maxY = center.getY() + y;
        double minZ = center.getZ() - z;
        double maxZ = center.getZ() + z;
        int minChunkX = (int) Math.floor(minX / 16.0D);
        int maxChunkX = (int) Math.floor(maxX / 16.0D);
        int minChunkZ = (int) Math.floor(minZ / 16.0D);
        int maxChunkZ = (int) Math.floor(maxZ / 16.0D);
        Collection<Entity> nearby = new ArrayList<>();
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                if (!world.isChunkLoaded(chunkX, chunkZ)) {
                    continue;
                }
                Entity[] entities = world.getChunkAt(chunkX, chunkZ).getEntities();
                for (int i = 0; i < entities.length; i++) {
                    Entity entity = entities[i];
                    if (entity == null || !entity.isValid()) {
                        continue;
                    }
                    Location location = entity.getLocation();
                    if (location.getX() >= minX
                            && location.getX() <= maxX
                            && location.getY() >= minY
                            && location.getY() <= maxY
                            && location.getZ() >= minZ
                            && location.getZ() <= maxZ) {
                        nearby.add(entity);
                    }
                }
            }
        }
        return nearby;
    }

    /** Typed nearby lookup without linking the Predicate {@code getNearbyEntities} overload. */
    public static <T extends Entity> Collection<T> nearbyEntitiesOfType(
            World world, Location center, double x, double y, double z, Class<T> type) {
        Objects.requireNonNull(type, "type");
        Collection<Entity> nearby = nearbyEntities(world, center, x, y, z, type::isInstance);
        if (nearby.isEmpty()) {
            return Collections.emptyList();
        }
        Collection<T> typed = new ArrayList<>(nearby.size());
        for (Entity entity : nearby) {
            typed.add(type.cast(entity));
        }
        return typed;
    }

    private static @Nullable Method resolve(String name) {
        try {
            return World.class.getMethod(name);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    private static @Nullable Method resolveNearby() {
        try {
            return World.class.getMethod(
                    "getNearbyEntities", Location.class, double.class, double.class, double.class);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    private static @Nullable Method resolveNearbyWithPredicate() {
        try {
            return World.class.getMethod(
                    "getNearbyEntities",
                    Location.class,
                    double.class,
                    double.class,
                    double.class,
                    Predicate.class);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    private static @Nullable Object invoke(@Nullable Method method, World world) {
        if (method == null) {
            return null;
        }
        try {
            return method.invoke(world);
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return null;
        }
    }
}
