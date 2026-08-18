package com.monkey.ultimatebot.bot.ai.services.cache;

public final class TargetCacheStats {

    private static final TargetCacheStats EMPTY = new TargetCacheStats(0L, 0L, 0L);

    private final long hitCount;
    private final long missCount;
    private final long evictionCount;

    public TargetCacheStats(long hitCount, long missCount, long evictionCount) {
        this.hitCount = hitCount;
        this.missCount = missCount;
        this.evictionCount = evictionCount;
    }

    public static TargetCacheStats empty() {
        return EMPTY;
    }

    public TargetCacheStats plus(TargetCacheStats other) {
        return new TargetCacheStats(
                hitCount + other.hitCount, missCount + other.missCount, evictionCount + other.evictionCount);
    }

    public long hitCount() {
        return hitCount;
    }

    public long missCount() {
        return missCount;
    }

    public long evictionCount() {
        return evictionCount;
    }
}
