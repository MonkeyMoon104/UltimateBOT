package com.monkey.ultimatebot.sdk.model.response;

/** Number of active bots currently tracked by the server. */
public final class BotCountResponse {
    private final int count;

    public BotCountResponse(int count) {

        if (count < 0) {
            throw new IllegalArgumentException("count cannot be negative");
        }
        this.count = count;
    }

    public int count() {
        return count;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BotCountResponse)) {
            return false;
        }
        BotCountResponse other = (BotCountResponse) obj;
        return count == other.count;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(count);
    }

    @Override
    public String toString() {
        return "BotCountResponse[count=" + count + "]";
    }
}
