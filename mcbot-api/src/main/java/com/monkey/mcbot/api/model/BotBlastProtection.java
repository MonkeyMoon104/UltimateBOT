package com.monkey.mcbot.api.model;

/**
 * Per-armor-piece blast protection profile.
 *
 * @param feet {@code true} to enable blast protection on boots
 * @param legs {@code true} to enable blast protection on leggings
 * @param chest {@code true} to enable blast protection on chestplate
 * @param head {@code true} to enable blast protection on helmet
 */
public record BotBlastProtection(
        boolean feet,
        boolean legs,
        boolean chest,
        boolean head
) {
    /**
     * Creates a profile where all armor pieces share the same state.
     *
     * @param enabled global state to apply on all four pieces
     * @return blast profile with uniform value
     */
    public static BotBlastProtection all(boolean enabled) {
        return new BotBlastProtection(enabled, enabled, enabled, enabled);
    }

    /**
     * Creates a profile using booleans for each armor slot.
     *
     * @param bootsBlastEnabled boots blast state
     * @param leggingsBlastEnabled leggings blast state
     * @param chestplateBlastEnabled chestplate blast state
     * @param helmetBlastEnabled helmet blast state
     * @return blast profile
     */
    public static BotBlastProtection of(boolean bootsBlastEnabled,
                                        boolean leggingsBlastEnabled,
                                        boolean chestplateBlastEnabled,
                                        boolean helmetBlastEnabled) {
        return new BotBlastProtection(bootsBlastEnabled, leggingsBlastEnabled, chestplateBlastEnabled, helmetBlastEnabled);
    }

    /**
     * Creates a profile using binary integer values for each armor slot.
     *
     * <p>Accepted values are {@code 0} (disabled) and {@code 1} (enabled).</p>
     *
     * @param bootsBlastEnabled boots blast state, {@code 0} or {@code 1}
     * @param leggingsBlastEnabled leggings blast state, {@code 0} or {@code 1}
     * @param chestplateBlastEnabled chestplate blast state, {@code 0} or {@code 1}
     * @param helmetBlastEnabled helmet blast state, {@code 0} or {@code 1}
     * @return blast profile
     * @throws IllegalArgumentException if any value is not {@code 0} or {@code 1}
     */
    public static BotBlastProtection of(int bootsBlastEnabled,
                                        int leggingsBlastEnabled,
                                        int chestplateBlastEnabled,
                                        int helmetBlastEnabled) {
        return new BotBlastProtection(
                parseBinary(bootsBlastEnabled, "boots"),
                parseBinary(leggingsBlastEnabled, "leggings"),
                parseBinary(chestplateBlastEnabled, "chestplate"),
                parseBinary(helmetBlastEnabled, "helmet")
        );
    }

    /**
     * Checks whether blast protection is enabled for all pieces.
     *
     * @return {@code true} only when all four slots are enabled
     */
    public boolean allEnabled() {
        return feet && legs && chest && head;
    }

    private static boolean parseBinary(int value, String field) {
        if (value == 0) {
            return false;
        }
        if (value == 1) {
            return true;
        }
        throw new IllegalArgumentException(field + " blast value must be 0 or 1");
    }
}
