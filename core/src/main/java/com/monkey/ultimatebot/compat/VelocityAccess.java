package com.monkey.ultimatebot.compat;

import java.util.Objects;
import org.bukkit.util.Vector;

/**
 * Keeps Bukkit {@link Vector} motion finite.
 *
 * <p>{@code Vector#normalize()} of a zero delta (bot teleported onto the target) yields NaN. Paper
 * 1.16.3 {@code EntityTrackerEntry} then calls {@code CraftEntity#setVelocity} and crashes the
 * world tick. 1.17+ is unchanged for already-finite motion.
 */
public final class VelocityAccess {

    private VelocityAccess() {}

    public static Vector finite(Vector velocity) {
        Objects.requireNonNull(velocity, "velocity");
        double x = velocity.getX();
        double y = velocity.getY();
        double z = velocity.getZ();
        if (Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(z)) {
            return velocity;
        }
        return new Vector(
                Double.isFinite(x) ? x : 0.0D, Double.isFinite(y) ? y : 0.0D, Double.isFinite(z) ? z : 0.0D);
    }
}
