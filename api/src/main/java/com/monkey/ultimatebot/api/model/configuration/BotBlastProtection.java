package com.monkey.ultimatebot.api.model.configuration;

/**
 * Per-armor-piece blast protection profile.
 *
 * @param feet {@code true} to enable blast protection on boots
 * @param legs {@code true} to enable blast protection on leggings
 * @param chest {@code true} to enable blast protection on chestplate
 * @param head {@code true} to enable blast protection on helmet
 */
public final class BotBlastProtection {
    private final boolean feet;
    private final boolean legs;
    private final boolean chest;
    private final boolean head;

    public BotBlastProtection(boolean feet, boolean legs, boolean chest, boolean head) {
        this.feet = feet;
        this.legs = legs;
        this.chest = chest;
        this.head = head;
    }

    public boolean feet() {
        return feet;
    }

    public boolean legs() {
        return legs;
    }

    public boolean chest() {
        return chest;
    }

    public boolean head() {
        return head;
    }

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
    public static BotBlastProtection of(
            boolean bootsBlastEnabled,
            boolean leggingsBlastEnabled,
            boolean chestplateBlastEnabled,
            boolean helmetBlastEnabled) {
        return new BotBlastProtection(
                bootsBlastEnabled, leggingsBlastEnabled, chestplateBlastEnabled, helmetBlastEnabled);
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
    public static BotBlastProtection of(
            int bootsBlastEnabled, int leggingsBlastEnabled, int chestplateBlastEnabled, int helmetBlastEnabled) {
        return new BotBlastProtection(
                parseBinary(bootsBlastEnabled, "boots"),
                parseBinary(leggingsBlastEnabled, "leggings"),
                parseBinary(chestplateBlastEnabled, "chestplate"),
                parseBinary(helmetBlastEnabled, "helmet"));
    }

    /**
     * Checks whether blast protection is enabled for all pieces.
     *
     * @return {@code true} only when all four slots are enabled
     */
    public boolean allEnabled() {
        return feet && legs && chest && head;
    }

    /** Converts this Bukkit API model to the platform-independent representation. */
    public com.monkey.ultimatebot.common.model.BlastProtectionSettings toCommon() {
        return new com.monkey.ultimatebot.common.model.BlastProtectionSettings(feet, legs, chest, head);
    }

    /** Creates the Bukkit API model from the platform-independent representation. */
    public static BotBlastProtection fromCommon(com.monkey.ultimatebot.common.model.BlastProtectionSettings settings) {
        java.util.Objects.requireNonNull(settings, "settings");
        return new BotBlastProtection(settings.boots(), settings.leggings(), settings.chestplate(), settings.helmet());
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BotBlastProtection)) {
            return false;
        }
        BotBlastProtection other = (BotBlastProtection) obj;
        return feet == other.feet && legs == other.legs && chest == other.chest && head == other.head;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(feet, legs, chest, head);
    }

    @Override
    public String toString() {
        return "BotBlastProtection[feet=" + feet + ", legs=" + legs + ", chest=" + chest + ", head=" + head + "]";
    }
}
