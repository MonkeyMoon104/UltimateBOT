package com.monkey.ultimatebot.common.model;

/** Immutable automatic-targeting configuration. */
public record AutoTargetSettings(boolean enabled, double range) {
    public AutoTargetSettings {
        if (!Double.isFinite(range) || range <= 0.0D) {
            throw new IllegalArgumentException("range must be finite and greater than zero");
        }
    }
}
