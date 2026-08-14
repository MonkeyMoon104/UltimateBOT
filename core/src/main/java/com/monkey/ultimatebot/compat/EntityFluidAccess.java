package com.monkey.ultimatebot.compat;

import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

/**
 * Water immersion across Paper versions and fake-player edge cases.
 *
 * <p>{@link Entity#isInWater()} is 1.16+ and can stay false on fake {@code EntityPlayer}s. The
 * invoke is gated so 1.15 class-load never links it; block sampling is the dual-path fallback.
 */
public final class EntityFluidAccess {

    private static final boolean ENTITY_IS_IN_WATER = hasMethod(Entity.class, "isInWater");
    private static final boolean HAS_WATERLOGGED = hasClass("org.bukkit.block.data.Waterlogged");

    private EntityFluidAccess() {}

    /**
     * Any contact with water (feet or eyes). Used by Water PvP to start fighting at the surface.
     */
    public static boolean isInWater(Entity entity) {
        Objects.requireNonNull(entity, "entity");
        if (ENTITY_IS_IN_WATER && entity.isInWater()) {
            return true;
        }
        Location feet = Objects.requireNonNull(entity.getLocation(), "location");
        if (isWatery(feet.getBlock())) {
            return true;
        }
        return isEyesInWater(entity);
    }

    /**
     * Head fully underwater. Trident may still fight with feet-only water; full submersion suspends
     * non-Water modes (anti-fly).
     */
    public static boolean isFullySubmerged(Entity entity) {
        Objects.requireNonNull(entity, "entity");
        return isEyesInWater(entity);
    }

    private static boolean isEyesInWater(Entity entity) {
        if (entity instanceof LivingEntity) {
            Location eyes =
                    Objects.requireNonNull(((LivingEntity) entity).getEyeLocation(), "eye location");
            return isWatery(eyes.getBlock());
        }
        Location feet = Objects.requireNonNull(entity.getLocation(), "location");
        return isWatery(feet.clone().add(0.0D, Math.max(0.9D, entity.getHeight() * 0.9D), 0.0D).getBlock());
    }

    private static boolean isWatery(@Nullable Block block) {
        if (block == null) {
            return false;
        }
        Material type = block.getType();
        if (type == Material.WATER || type.name().equals("BUBBLE_COLUMN")) {
            return true;
        }
        if (!HAS_WATERLOGGED) {
            return false;
        }
        BlockData data = block.getBlockData();
        return data instanceof Waterlogged && ((Waterlogged) data).isWaterlogged();
    }

    private static boolean hasMethod(Class<?> type, String name) {
        try {
            type.getMethod(name);
            return true;
        } catch (NoSuchMethodException ignored) {
            return false;
        }
    }

    private static boolean hasClass(String name) {
        try {
            Class.forName(name);
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }
}
