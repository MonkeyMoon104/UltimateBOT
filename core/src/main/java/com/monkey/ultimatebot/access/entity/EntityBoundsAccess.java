package com.monkey.ultimatebot.access.entity;

import java.lang.reflect.Method;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

public final class EntityBoundsAccess {

    private static final double PLAYER_WIDTH = 0.6D;
    private static final double PLAYER_HEIGHT = 1.8D;
    private static final double ENTITY_WIDTH = 0.6D;
    private static final double ENTITY_HEIGHT = 1.8D;

    private static final @Nullable Method GET_BOUNDING_BOX = resolveGetBoundingBox();
    private static final @Nullable Method GET_HEIGHT = resolveGetHeight();
    private static final @Nullable Method GET_MIN_X = resolveBoxGetter("getMinX");
    private static final @Nullable Method GET_MIN_Y = resolveBoxGetter("getMinY");
    private static final @Nullable Method GET_MIN_Z = resolveBoxGetter("getMinZ");
    private static final @Nullable Method GET_MAX_X = resolveBoxGetter("getMaxX");
    private static final @Nullable Method GET_MAX_Y = resolveBoxGetter("getMaxY");
    private static final @Nullable Method GET_MAX_Z = resolveBoxGetter("getMaxZ");

    private EntityBoundsAccess() {}

    public static Box of(Entity entity) {
        Objects.requireNonNull(entity, "entity");
        Box modern = modernBox(entity);
        if (modern != null) {
            return modern;
        }
        return approximate(entity);
    }

    public static double height(Entity entity) {
        Objects.requireNonNull(entity, "entity");
        if (GET_HEIGHT != null) {
            try {
                Object result = GET_HEIGHT.invoke(entity);
                if (result instanceof Number) {
                    return ((Number) result).doubleValue();
                }
            } catch (ReflectiveOperationException | LinkageError ignored) {

            }
        }
        Box box = of(entity);
        return Math.max(0.1D, box.getMaxY() - box.getMinY());
    }

    private static @Nullable Box modernBox(Entity entity) {
        if (GET_BOUNDING_BOX == null
                || GET_MIN_X == null
                || GET_MIN_Y == null
                || GET_MIN_Z == null
                || GET_MAX_X == null
                || GET_MAX_Y == null
                || GET_MAX_Z == null) {
            return null;
        }
        try {
            Object box = GET_BOUNDING_BOX.invoke(entity);
            if (box == null) {
                return null;
            }
            return new Box(
                    ((Number) GET_MIN_X.invoke(box)).doubleValue(),
                    ((Number) GET_MIN_Y.invoke(box)).doubleValue(),
                    ((Number) GET_MIN_Z.invoke(box)).doubleValue(),
                    ((Number) GET_MAX_X.invoke(box)).doubleValue(),
                    ((Number) GET_MAX_Y.invoke(box)).doubleValue(),
                    ((Number) GET_MAX_Z.invoke(box)).doubleValue());
        } catch (ReflectiveOperationException | LinkageError | ClassCastException ignored) {
            return null;
        }
    }

    private static Box approximate(Entity entity) {
        Location loc = entity.getLocation();
        double width = entity instanceof Player ? PLAYER_WIDTH : ENTITY_WIDTH;
        double height = entity instanceof Player ? PLAYER_HEIGHT : ENTITY_HEIGHT;
        if (entity instanceof Player) {
            Player player = (Player) entity;
            try {
                if (player.isSneaking()) {
                    height = 1.5D;
                }
            } catch (NoSuchMethodError ignored) {

            }
        }
        double half = width * 0.5D;
        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();
        return new Box(x - half, y, z - half, x + half, y + height, z + half);
    }

    private static @Nullable Method resolveGetBoundingBox() {
        try {
            return Entity.class.getMethod("getBoundingBox");
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    private static @Nullable Method resolveGetHeight() {
        try {
            return Entity.class.getMethod("getHeight");
        } catch (NoSuchMethodException ignored) {
            try {
                return Class.forName("org.bukkit.entity.LivingEntity").getMethod("getHeight");
            } catch (ClassNotFoundException | NoSuchMethodException missing) {
                return null;
            }
        }
    }

    private static @Nullable Method resolveBoxGetter(String name) {
        try {
            Class<?> box = Class.forName("org.bukkit.util.BoundingBox");
            return box.getMethod(name);
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
            return null;
        }
    }

    public static final class Box {
        private final double minX;
        private final double minY;
        private final double minZ;
        private final double maxX;
        private final double maxY;
        private final double maxZ;

        public Box(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
        }

        public double getMinX() {
            return minX;
        }

        public double getMinY() {
            return minY;
        }

        public double getMinZ() {
            return minZ;
        }

        public double getMaxX() {
            return maxX;
        }

        public double getMaxY() {
            return maxY;
        }

        public double getMaxZ() {
            return maxZ;
        }

        public double getWidthX() {
            return maxX - minX;
        }
    }
}
