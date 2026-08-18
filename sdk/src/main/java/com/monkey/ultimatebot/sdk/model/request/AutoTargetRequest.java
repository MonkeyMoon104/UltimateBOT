package com.monkey.ultimatebot.sdk.model.request;

/** Runtime automatic-targeting configuration. */
public final class AutoTargetRequest {
    private final boolean enabled;
    private final double range;

    public AutoTargetRequest(boolean enabled, double range) {

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
        if (!(obj instanceof AutoTargetRequest)) {
            return false;
        }
        AutoTargetRequest other = (AutoTargetRequest) obj;
        return enabled == other.enabled && Double.compare(range, other.range) == 0;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(enabled, range);
    }

    @Override
    public String toString() {
        return "AutoTargetRequest[enabled=" + enabled + ", range=" + range + "]";
    }
}
