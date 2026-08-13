package com.monkey.ultimatebot.sdk.model.request;

/**
 * Boolean patch request used by remote feature toggles.
 *
 * @param enabled target enabled state
 */
public final class ToggleRequest {
    private final boolean enabled;

    public ToggleRequest(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean enabled() {
        return enabled;
    }

    /**
     * Creates an enabled toggle request.
     *
     * @return enabled request
     */
    public static ToggleRequest enable() {
        return new ToggleRequest(true);
    }

    /**
     * Creates a disabled toggle request.
     *
     * @return disabled request
     */
    public static ToggleRequest disable() {
        return new ToggleRequest(false);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ToggleRequest)) {
            return false;
        }
        ToggleRequest other = (ToggleRequest) obj;
        return enabled == other.enabled;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(enabled);
    }

    @Override
    public String toString() {
        return "ToggleRequest[enabled=" + enabled + "]";
    }
}
