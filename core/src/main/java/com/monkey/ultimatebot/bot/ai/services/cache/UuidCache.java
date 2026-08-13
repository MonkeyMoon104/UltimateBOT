package com.monkey.ultimatebot.bot.ai.services.cache;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** Small cache facade so TargetingService never links Caffeine types at class-load time. */
public interface UuidCache<V> {

    @Nullable V getIfPresent(UUID key);

    void put(UUID key, V value);

    void invalidate(UUID key);

    void invalidateAll();

    TargetCacheStats stats();

    long estimatedSize();
}
