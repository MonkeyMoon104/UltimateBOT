package com.monkey.ultimatebot.common.model;

/** Immutable idle wandering and return-to-spawn configuration. */
public record IdleWanderSettings(boolean enabled, double radius, double returnDistance, long returnDelayMs) {
    public IdleWanderSettings {
        if (!Double.isFinite(radius) || radius <= 0.0D) {
            throw new IllegalArgumentException("radius must be finite and greater than zero");
        }
        if (!Double.isFinite(returnDistance) || returnDistance <= 0.0D) {
            throw new IllegalArgumentException("returnDistance must be finite and greater than zero");
        }
        if (returnDelayMs < 0L) {
            throw new IllegalArgumentException("returnDelayMs cannot be negative");
        }
    }
}
