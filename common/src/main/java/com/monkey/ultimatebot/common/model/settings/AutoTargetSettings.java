package com.monkey.ultimatebot.common.model.settings;

public final class AutoTargetSettings {
    private final boolean enabled;
    private final double range;

    public AutoTargetSettings(boolean enabled, double range) {
        if (!Double.isFinite(range) || range <= 0.0D) {
            throw new IllegalArgumentException("range must be finite and greater than zero");
        }
        this.enabled = enabled;
        this.range = range;
    }

    public boolean enabled() {
        return enabled;
    }

    public double range() {
        return range;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AutoTargetSettings)) {
            return false;
        }
        AutoTargetSettings other = (AutoTargetSettings) obj;
        return enabled == other.enabled && Double.compare(range, other.range) == 0;
    }

    @Override
    public int hashCode() {
        return 31 * Boolean.hashCode(enabled) + Double.hashCode(range);
    }

    @Override
    public String toString() {
        return "AutoTargetSettings[enabled=" + enabled + ", range=" + range + ']';
    }
}
