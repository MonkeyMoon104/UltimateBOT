package com.monkey.ultimatebot.common.model;

public final class BlastProtectionSettings {
    private final boolean boots;
    private final boolean leggings;
    private final boolean chestplate;
    private final boolean helmet;

    public BlastProtectionSettings(boolean boots, boolean leggings, boolean chestplate, boolean helmet) {
        this.boots = boots;
        this.leggings = leggings;
        this.chestplate = chestplate;
        this.helmet = helmet;
    }

    public static BlastProtectionSettings all(boolean enabled) {
        return new BlastProtectionSettings(enabled, enabled, enabled, enabled);
    }

    public boolean boots() {
        return boots;
    }

    public boolean leggings() {
        return leggings;
    }

    public boolean chestplate() {
        return chestplate;
    }

    public boolean helmet() {
        return helmet;
    }

    public boolean fullyEnabled() {
        return boots && leggings && chestplate && helmet;
    }

    public boolean anyEnabled() {
        return boots || leggings || chestplate || helmet;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BlastProtectionSettings)) {
            return false;
        }
        BlastProtectionSettings other = (BlastProtectionSettings) obj;
        return boots == other.boots
                && leggings == other.leggings
                && chestplate == other.chestplate
                && helmet == other.helmet;
    }

    @Override
    public int hashCode() {
        int result = Boolean.hashCode(boots);
        result = 31 * result + Boolean.hashCode(leggings);
        result = 31 * result + Boolean.hashCode(chestplate);
        result = 31 * result + Boolean.hashCode(helmet);
        return result;
    }

    @Override
    public String toString() {
        return "BlastProtectionSettings[boots="
                + boots
                + ", leggings="
                + leggings
                + ", chestplate="
                + chestplate
                + ", helmet="
                + helmet
                + ']';
    }
}
