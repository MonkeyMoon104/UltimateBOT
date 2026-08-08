package com.monkey.ultimatebot.bot.ai.controllers.movement.helper;

import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

/** Smooths horizontal direction changes while preserving deliberate A* steering. */
final class PathSteering {
    private static final double SHARP_TURN_DOT_THRESHOLD = 0.35D;
    private static final double GENTLE_TURN_BLEND = 0.32D;
    private static final double SHARP_TURN_BLEND = 0.55D;
    private static final double MIN_DIRECTION_LENGTH_SQUARED = 1.0E-6D;

    private @Nullable Vector direction;

    Vector update(Vector currentVelocity, Vector desiredDirection) {
        Vector desired = horizontalNormalized(desiredDirection);
        if (desired.lengthSquared() < MIN_DIRECTION_LENGTH_SQUARED) {
            return new Vector();
        }

        Vector previous = direction;
        if (previous == null || previous.lengthSquared() < MIN_DIRECTION_LENGTH_SQUARED) {
            previous = horizontalNormalized(currentVelocity);
        }
        if (previous.lengthSquared() < MIN_DIRECTION_LENGTH_SQUARED) {
            direction = desired;
            return desired;
        }

        double blend = previous.dot(desired) < SHARP_TURN_DOT_THRESHOLD ? SHARP_TURN_BLEND : GENTLE_TURN_BLEND;
        Vector blended = previous.clone().multiply(1.0D - blend).add(desired.clone().multiply(blend));
        direction = horizontalNormalized(blended);
        if (direction.lengthSquared() < MIN_DIRECTION_LENGTH_SQUARED) {
            direction = desired;
        }
        return direction;
    }

    void reset() {
        direction = null;
    }

    private static Vector horizontalNormalized(Vector vector) {
        Vector horizontal = new Vector(vector.getX(), 0.0D, vector.getZ());
        return horizontal.lengthSquared() < MIN_DIRECTION_LENGTH_SQUARED ? new Vector() : horizontal.normalize();
    }
}
