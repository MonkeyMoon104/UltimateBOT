package com.monkey.ultimatebot.sdk.model.request;

/** Runtime idle movement and return-to-spawn configuration. */
public record IdleWanderRequest(boolean enabled, double radius, double returnDistance, long returnDelayMs) {
    public IdleWanderRequest {
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
