package com.monkey.ultimatebot.combat.mode.water;

import java.util.Objects;
import org.bukkit.util.Vector;

public final class UnderwaterMotionPlanner {
    private static final double MOMENTUM_WEIGHT = 0.15D;
    private static final double DESIRED_WEIGHT = 1.0D - MOMENTUM_WEIGHT;

    private UnderwaterMotionPlanner() {}

    public static Vector pursue(Vector origin, Vector target, Vector currentVelocity, double speed) {
        double checkedSpeed = requirePositive(speed, "speed");
        Vector delta = Objects.requireNonNull(target, "target").clone().subtract(Objects.requireNonNull(origin, "origin"));
        if (delta.lengthSquared() < 1.0E-6D) {
            return Objects.requireNonNull(currentVelocity, "currentVelocity").clone().multiply(MOMENTUM_WEIGHT);
        }
        Vector desired = delta.normalize().multiply(checkedSpeed);
        return blendAndLimit(currentVelocity, desired, checkedSpeed);
    }

    public static Vector orbit(
            Vector origin,
            Vector target,
            Vector currentVelocity,
            double radialSpeed,
            double strafeSpeed,
            double strafeDirection) {
        Vector delta = Objects.requireNonNull(target, "target").clone().subtract(Objects.requireNonNull(origin, "origin"));
        Vector horizontal = new Vector(delta.getX(), 0.0D, delta.getZ());
        if (horizontal.lengthSquared() < 1.0E-6D) {
            horizontal = new Vector(1.0D, 0.0D, 0.0D);
        } else {
            horizontal = horizontal.normalize();
        }
        double checkedStrafeSpeed = requireNonNegative(strafeSpeed, "strafeSpeed");
        double side = strafeDirection < 0.0D ? -1.0D : 1.0D;
        Vector lateral = new Vector(-horizontal.getZ(), 0.0D, horizontal.getX()).multiply(checkedStrafeSpeed * side);
        double verticalCorrection = Math.min(0.16D, Math.max(-0.16D, delta.getY() * 0.14D));
        Vector desired = horizontal.multiply(radialSpeed).add(lateral).add(new Vector(0.0D, verticalCorrection, 0.0D));
        double limit = Math.max(0.12D, Math.hypot(radialSpeed, checkedStrafeSpeed) + 0.04D);
        return blendAndLimit(currentVelocity, desired, limit);
    }

    private static Vector blendAndLimit(Vector currentVelocity, Vector desired, double maximumSpeed) {
        Vector blended = Objects.requireNonNull(currentVelocity, "currentVelocity")
                .clone()
                .multiply(MOMENTUM_WEIGHT)
                .add(desired.clone().multiply(DESIRED_WEIGHT));
        double maximumSquared = maximumSpeed * maximumSpeed;
        return blended.lengthSquared() > maximumSquared ? blended.normalize().multiply(maximumSpeed) : blended;
    }

    private static double requirePositive(double value, String name) {
        if (!Double.isFinite(value) || value <= 0.0D) {
            throw new IllegalArgumentException(name + " must be finite and positive");
        }
        return value;
    }

    private static double requireNonNegative(double value, String name) {
        if (!Double.isFinite(value) || value < 0.0D) {
            throw new IllegalArgumentException(name + " must be finite and non-negative");
        }
        return value;
    }
}
