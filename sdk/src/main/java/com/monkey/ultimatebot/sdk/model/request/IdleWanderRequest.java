package com.monkey.ultimatebot.sdk.model.request;

/** Runtime idle movement and return-to-spawn configuration. */
public final class IdleWanderRequest {
    private final boolean enabled;
    private final double radius;
    private final double returnDistance;
    private final long returnDelayMs;

    public IdleWanderRequest(boolean enabled, double radius, double returnDistance, long returnDelayMs) {


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
        if (!(obj instanceof IdleWanderRequest)) {
            return false;
        }
        IdleWanderRequest other = (IdleWanderRequest) obj;
        return enabled == other.enabled && Double.compare(radius, other.radius) == 0 && Double.compare(returnDistance, other.returnDistance) == 0 && returnDelayMs == other.returnDelayMs;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(enabled, radius, returnDistance, returnDelayMs);
    }

    @Override
    public String toString() {
        return "IdleWanderRequest[enabled=" + enabled + ", radius=" + radius + ", returnDistance=" + returnDistance + ", returnDelayMs=" + returnDelayMs + "]";
    }
}
