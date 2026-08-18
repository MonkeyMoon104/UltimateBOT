package com.monkey.ultimatebot.access.entity;

import java.lang.reflect.Method;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

public final class EntityFluidAccess {

    private static final boolean ENTITY_IS_IN_WATER = hasMethod(Entity.class, "isInWater");
    private static final boolean HAS_WATERLOGGED = hasClass("org.bukkit.block.data.Waterlogged");
    private static final @Nullable Method GET_BLOCK_DATA = method(Block.class, "getBlockData");
    private static final @Nullable Class<?> WATERLOGGED_TYPE = classOrNull("org.bukkit.block.data.Waterlogged");
    private static final @Nullable Method IS_WATERLOGGED = method(WATERLOGGED_TYPE, "isWaterlogged");

    private EntityFluidAccess() {}

    public static boolean isInWater(Entity entity) {
        Objects.requireNonNull(entity, "entity");
        if (ENTITY_IS_IN_WATER && invokeIsInWater(entity)) {
            return true;
        }
        Location feet = Objects.requireNonNull(entity.getLocation(), "location");
        if (isWatery(feet.getBlock())) {
            return true;
        }
        return isEyesInWater(entity);
    }

    public static boolean isFullySubmerged(Entity entity) {
        Objects.requireNonNull(entity, "entity");
        return isEyesInWater(entity);
    }

    private static boolean isEyesInWater(Entity entity) {
        if (entity instanceof LivingEntity) {
            Location eyes = Objects.requireNonNull(((LivingEntity) entity).getEyeLocation(), "eye location");
            return isWatery(eyes.getBlock());
        }
        Location feet = Objects.requireNonNull(entity.getLocation(), "location");
        return isWatery(feet.clone()
                .add(0.0D, Math.max(0.9D, EntityBoundsAccess.height(entity) * 0.9D), 0.0D)
                .getBlock());
    }

    private static boolean isWatery(@Nullable Block block) {
        if (block == null) {
            return false;
        }
        Material type = block.getType();
        String name = type.name();

        if (type == Material.WATER || "STATIONARY_WATER".equals(name) || "BUBBLE_COLUMN".equals(name)) {
            return true;
        }
        if (!HAS_WATERLOGGED || GET_BLOCK_DATA == null || WATERLOGGED_TYPE == null || IS_WATERLOGGED == null) {
            return false;
        }
        try {
            Object data = GET_BLOCK_DATA.invoke(block);
            if (data == null || !WATERLOGGED_TYPE.isInstance(data)) {
                return false;
            }
            Object waterlogged = IS_WATERLOGGED.invoke(data);
            return waterlogged instanceof Boolean && ((Boolean) waterlogged).booleanValue();
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return false;
        }
    }

    private static boolean invokeIsInWater(Entity entity) {
        try {

            return entity.isInWater();
        } catch (NoSuchMethodError ignored) {
            return false;
        }
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
        return classOrNull(name) != null;
    }

    private static @Nullable Class<?> classOrNull(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException | LinkageError ignored) {
            return null;
        }
    }

    private static @Nullable Method method(@Nullable Class<?> type, String name, Class<?>... params) {
        if (type == null) {
            return null;
        }
        try {
            return type.getMethod(name, params);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }
}
