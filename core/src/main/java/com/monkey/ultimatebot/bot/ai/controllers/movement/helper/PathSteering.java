package com.monkey.ultimatebot.bot.ai.controllers.movement.helper;

import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Smooths horizontal direction changes while preserving deliberate A* steering. */
final class PathSteering {
    private static final double SHARP_TURN_DOT_THRESHOLD = 0.35D;
    private static final double GENTLE_TURN_BLEND = 0.32D;
    private static final double SHARP_TURN_BLEND = 0.55D;
    private static final double MIN_DIRECTION_LENGTH_SQUARED = 1.0E-6D;

    private @Nullable Vec3 direction;

    Vec3 update(Vec3 currentVelocity, Vec3 desiredDirection) {
        Vec3 desired = horizontalNormalized(desiredDirection);
        if (desired.lengthSqr() < MIN_DIRECTION_LENGTH_SQUARED) {
            return Vec3.ZERO;
        }

        Vec3 previous = direction;
        if (previous == null || previous.lengthSqr() < MIN_DIRECTION_LENGTH_SQUARED) {
            previous = horizontalNormalized(currentVelocity);
        }
        if (previous.lengthSqr() < MIN_DIRECTION_LENGTH_SQUARED) {
            direction = desired;
            return desired;
        }

        double blend = previous.dot(desired) < SHARP_TURN_DOT_THRESHOLD ? SHARP_TURN_BLEND : GENTLE_TURN_BLEND;
        Vec3 blended = previous.scale(1.0D - blend).add(desired.scale(blend));
        direction = horizontalNormalized(blended);
        if (direction.lengthSqr() < MIN_DIRECTION_LENGTH_SQUARED) {
            direction = desired;
        }
        return direction;
    }

    void reset() {
        direction = null;
    }

    private static Vec3 horizontalNormalized(Vec3 vector) {
        Vec3 horizontal = new Vec3(vector.x, 0.0D, vector.z);
        return horizontal.lengthSqr() < MIN_DIRECTION_LENGTH_SQUARED ? Vec3.ZERO : horizontal.normalize();
    }
}
