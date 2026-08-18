package com.monkey.ultimatebot.common.model;

import com.monkey.ultimatebot.common.util.TextValues;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

public final class KillMessageSettings {
    private final boolean enabled;
    private final @Nullable String message;

    public KillMessageSettings(boolean enabled, @Nullable String message) {
        if (enabled && message != null && TextValues.isBlank(message)) {
            throw new IllegalArgumentException("message cannot be blank when provided");
        }
        this.enabled = enabled;
        this.message = message;
    }

    public static KillMessageSettings disabled() {
        return new KillMessageSettings(false, null);
    }

    public boolean enabled() {
        return enabled;
    }

    public @Nullable String message() {
        return message;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof KillMessageSettings)) {
            return false;
        }
        KillMessageSettings other = (KillMessageSettings) obj;
        return enabled == other.enabled && Objects.equals(message, other.message);
    }

    @Override
    public int hashCode() {
        return 31 * Boolean.hashCode(enabled) + Objects.hashCode(message);
    }

    @Override
    public String toString() {
        return "KillMessageSettings[enabled=" + enabled + ", message=" + message + ']';
    }
}
