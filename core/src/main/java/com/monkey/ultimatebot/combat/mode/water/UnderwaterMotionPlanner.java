package com.monkey.ultimatebot.combat.mode.water;

import java.util.Objects;
import net.minecraft.world.phys.Vec3;

public final class UnderwaterMotionPlanner {
    private static final double MOMENTUM_WEIGHT = 0.15D;
    private static final double DESIRED_WEIGHT = 1.0D - MOMENTUM_WEIGHT;

    private UnderwaterMotionPlanner() {}

    public static Vec3 pursue(Vec3 origin, Vec3 target, Vec3 currentVelocity, double speed) {
        double checkedSpeed = requirePositive(speed, "speed");
        Vec3 delta = Objects.requireNonNull(target, "target").subtract(Objects.requireNonNull(origin, "origin"));
        if (delta.lengthSqr() < 1.0E-6D) {
            return Objects.requireNonNull(currentVelocity, "currentVelocity").scale(MOMENTUM_WEIGHT);
        }
        Vec3 desired = delta.normalize().scale(checkedSpeed);
        return blendAndLimit(currentVelocity, desired, checkedSpeed);
    }

    public static Vec3 orbit(
            Vec3 origin,
            Vec3 target,
            Vec3 currentVelocity,
            double radialSpeed,
            double strafeSpeed,
            double strafeDirection) {
        Vec3 delta = Objects.requireNonNull(target, "target").subtract(Objects.requireNonNull(origin, "origin"));
        Vec3 horizontal = new Vec3(delta.x, 0.0D, delta.z);
        if (horizontal.lengthSqr() < 1.0E-6D) {
            horizontal = new Vec3(1.0D, 0.0D, 0.0D);
        } else {
            horizontal = horizontal.normalize();
        }
        double checkedStrafeSpeed = requireNonNegative(strafeSpeed, "strafeSpeed");
        double side = strafeDirection < 0.0D ? -1.0D : 1.0D;
        Vec3 lateral = new Vec3(-horizontal.z, 0.0D, horizontal.x).scale(checkedStrafeSpeed * side);
        double verticalCorrection = Math.clamp(delta.y * 0.14D, -0.16D, 0.16D);
        Vec3 desired = horizontal.scale(radialSpeed).add(lateral).add(0.0D, verticalCorrection, 0.0D);
        double limit = Math.max(0.12D, Math.hypot(radialSpeed, checkedStrafeSpeed) + 0.04D);
        return blendAndLimit(currentVelocity, desired, limit);
    }

    private static Vec3 blendAndLimit(Vec3 currentVelocity, Vec3 desired, double maximumSpeed) {
        Vec3 blended = Objects.requireNonNull(currentVelocity, "currentVelocity")
                .scale(MOMENTUM_WEIGHT)
                .add(desired.scale(DESIRED_WEIGHT));
        double maximumSquared = maximumSpeed * maximumSpeed;
        return blended.lengthSqr() > maximumSquared ? blended.normalize().scale(maximumSpeed) : blended;
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
