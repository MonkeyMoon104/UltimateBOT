package com.monkey.ultimatebot.access.entity;

import java.util.Objects;
import org.bukkit.util.Vector;

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
        return new Vector(Double.isFinite(x) ? x : 0.0D, Double.isFinite(y) ? y : 0.0D, Double.isFinite(z) ? z : 0.0D);
    }
}
