package com.monkey.ultimatebot.config;

import java.time.Duration;
import java.util.Objects;

public record RuntimeSettings(CacheSettings targetCache, CacheSettings blockStateCache) {

    private static final CacheSettings DEFAULT_TARGET_CACHE = new CacheSettings(2_048, Duration.ofMillis(250));
    private static final CacheSettings DEFAULT_BLOCK_STATE_CACHE = new CacheSettings(1_000, Duration.ofSeconds(5));

    public RuntimeSettings {
        Objects.requireNonNull(targetCache, "targetCache");
        Objects.requireNonNull(blockStateCache, "blockStateCache");
    }

    public static RuntimeSettings defaults() {
        return new RuntimeSettings(DEFAULT_TARGET_CACHE, DEFAULT_BLOCK_STATE_CACHE);
    }

    public record CacheSettings(long maximumSize, Duration expireAfterWrite) {

        public CacheSettings {
            if (maximumSize <= 0) {
                throw new IllegalArgumentException("maximumSize must be positive");
            }
            Objects.requireNonNull(expireAfterWrite, "expireAfterWrite");
            if (expireAfterWrite.isZero() || expireAfterWrite.isNegative()) {
                throw new IllegalArgumentException("expireAfterWrite must be positive");
            }
        }
    }
}
