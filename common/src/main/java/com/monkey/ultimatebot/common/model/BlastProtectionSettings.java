package com.monkey.ultimatebot.common.model;

/** Per-piece blast-protection state shared by the API, remote protocol and SDK. */
public record BlastProtectionSettings(boolean boots, boolean leggings, boolean chestplate, boolean helmet) {

    /** Returns a state with the same value applied to every armor piece. */
    public static BlastProtectionSettings all(boolean enabled) {
        return new BlastProtectionSettings(enabled, enabled, enabled, enabled);
    }

    /** Returns whether every standard armor piece has blast protection enabled. */
    public boolean fullyEnabled() {
        return boots && leggings && chestplate && helmet;
    }

    /** Returns whether at least one armor piece has blast protection enabled. */
    public boolean anyEnabled() {
        return boots || leggings || chestplate || helmet;
    }
}
