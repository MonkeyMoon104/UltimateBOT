package com.monkey.ultimatebot.common.model;

import org.jspecify.annotations.Nullable;

/** Immutable built-in kill-message configuration. */
public record KillMessageSettings(boolean enabled, @Nullable String message) {
    public KillMessageSettings {
        if (enabled && message != null && message.isBlank()) {
            throw new IllegalArgumentException("message cannot be blank when provided");
        }
    }

    public static KillMessageSettings disabled() {
        return new KillMessageSettings(false, null);
    }
}
