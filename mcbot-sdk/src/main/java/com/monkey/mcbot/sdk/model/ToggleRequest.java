package com.monkey.mcbot.sdk.model;

/**
 * Boolean patch request used by remote feature toggles.
 *
 * @param enabled target enabled state
 */
public record ToggleRequest(boolean enabled) {
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
}
