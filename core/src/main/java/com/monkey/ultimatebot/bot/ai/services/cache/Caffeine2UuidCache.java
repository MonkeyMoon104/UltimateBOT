package com.monkey.ultimatebot.bot.ai.services.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.monkey.ultimatebot.config.RuntimeSettings;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public final class Caffeine2UuidCache<V> implements UuidCache<V> {

    private final Cache<UUID, V> cache;

    private Caffeine2UuidCache(Cache<UUID, V> cache) {
        this.cache = cache;
    }

    public static <V> UuidCache<V> create(RuntimeSettings.CacheSettings settings) {
        return new Caffeine2UuidCache<>(newCache(settings));
    }

    private static <V> Cache<UUID, V> newCache(RuntimeSettings.CacheSettings settings) {
        return Caffeine.newBuilder()
                .maximumSize(settings.maximumSize())
                .expireAfterWrite(settings.expireAfterWrite())
                .recordStats()
                .build();
    }

    @Override
    public @Nullable V getIfPresent(UUID key) {
        return cache.getIfPresent(key);
    }

    @Override
    public void put(UUID key, V value) {
        cache.put(key, value);
    }

    @Override
    public void invalidate(UUID key) {
        cache.invalidate(key);
    }

    @Override
    public void invalidateAll() {
        cache.invalidateAll();
    }

    @Override
    public TargetCacheStats stats() {
        com.github.benmanes.caffeine.cache.stats.CacheStats stats = cache.stats();
        return new TargetCacheStats(stats.hitCount(), stats.missCount(), stats.evictionCount());
    }

    @Override
    public long estimatedSize() {
        return cache.estimatedSize();
    }
}
