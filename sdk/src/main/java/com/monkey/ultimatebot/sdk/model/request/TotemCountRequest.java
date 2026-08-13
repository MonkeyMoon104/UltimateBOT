package com.monkey.ultimatebot.sdk.model.request;

/** Runtime request for a bot's active totem count. */
public final class TotemCountRequest {
    private final int totemCount;

    public TotemCountRequest(int totemCount) {
        this.totemCount = totemCount;
    }

    public int totemCount() {
        return totemCount;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TotemCountRequest)) {
            return false;
        }
        TotemCountRequest other = (TotemCountRequest) obj;
        return totemCount == other.totemCount;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(totemCount);
    }

    @Override
    public String toString() {
        return "TotemCountRequest[totemCount=" + totemCount + "]";
    }
}
