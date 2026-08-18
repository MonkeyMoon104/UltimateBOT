package com.monkey.ultimatebot.bot.ai.services.cache;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

public interface UuidCache<V> {

    @Nullable V getIfPresent(UUID key);

    void put(UUID key, V value);

    void invalidate(UUID key);

    void invalidateAll();

    TargetCacheStats stats();

    long estimatedSize();
}
