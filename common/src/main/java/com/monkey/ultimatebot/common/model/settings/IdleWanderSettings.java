package com.monkey.ultimatebot.common.model.settings;

public final class IdleWanderSettings {
    private final boolean enabled;
    private final double radius;
    private final double returnDistance;
    private final long returnDelayMs;

    public IdleWanderSettings(boolean enabled, double radius, double returnDistance, long returnDelayMs) {
        if (!Double.isFinite(radius) || radius <= 0.0D) {
            throw new IllegalArgumentException("radius must be finite and greater than zero");
        }
        if (!Double.isFinite(returnDistance) || returnDistance <= 0.0D) {
            throw new IllegalArgumentException("returnDistance must be finite and greater than zero");
        }
        if (returnDelayMs < 0L) {
            throw new IllegalArgumentException("returnDelayMs cannot be negative");
        }
        this.enabled = enabled;
        this.radius = radius;
        this.returnDistance = returnDistance;
        this.returnDelayMs = returnDelayMs;
    }

    public boolean enabled() {
        return enabled;
    }

    public double radius() {
        return radius;
    }

    public double returnDistance() {
        return returnDistance;
    }

    public long returnDelayMs() {
        return returnDelayMs;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof IdleWanderSettings)) {
            return false;
        }
        IdleWanderSettings other = (IdleWanderSettings) obj;
        return enabled == other.enabled
                && Double.compare(radius, other.radius) == 0
                && Double.compare(returnDistance, other.returnDistance) == 0
                && returnDelayMs == other.returnDelayMs;
    }

    @Override
    public int hashCode() {
        int result = Boolean.hashCode(enabled);
        result = 31 * result + Double.hashCode(radius);
        result = 31 * result + Double.hashCode(returnDistance);
        result = 31 * result + Long.hashCode(returnDelayMs);
        return result;
    }

    @Override
    public String toString() {
        return "IdleWanderSettings[enabled="
                + enabled
                + ", radius="
                + radius
                + ", returnDistance="
                + returnDistance
                + ", returnDelayMs="
                + returnDelayMs
                + ']';
    }
}
