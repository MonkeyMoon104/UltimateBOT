package com.monkey.ultimatebot.sdk.model.request;

/** Runtime automatic-targeting configuration. */
public record AutoTargetRequest(boolean enabled, double range) {
    public AutoTargetRequest {
        if (!Double.isFinite(range) || range <= 0.0D) {
            throw new IllegalArgumentException("range must be finite and greater than zero");
        }
    }
}
