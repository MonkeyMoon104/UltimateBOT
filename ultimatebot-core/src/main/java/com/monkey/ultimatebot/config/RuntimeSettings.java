package com.monkey.ultimatebot.config;

import java.time.Duration;
import java.util.Objects;

public record RuntimeSettings(
        CacheSettings targetCache, CacheSettings blockStateCache, WorldProtectionSettings worldProtection) {

    private static final CacheSettings DEFAULT_TARGET_CACHE = new CacheSettings(2_048, Duration.ofMillis(250));
    private static final CacheSettings DEFAULT_BLOCK_STATE_CACHE = new CacheSettings(1_000, Duration.ofSeconds(5));
    private static final WorldProtectionSettings DEFAULT_WORLD_PROTECTION =
            new WorldProtectionSettings(false, true, 0, 2_048, true);

    public RuntimeSettings {
        Objects.requireNonNull(targetCache, "targetCache");
        Objects.requireNonNull(blockStateCache, "blockStateCache");
        Objects.requireNonNull(worldProtection, "worldProtection");
    }

    public static RuntimeSettings defaults() {
        return new RuntimeSettings(DEFAULT_TARGET_CACHE, DEFAULT_BLOCK_STATE_CACHE, DEFAULT_WORLD_PROTECTION);
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

    public record WorldProtectionSettings(
            boolean blockDamage,
            boolean antiDupe,
            int combatBlockLifetimeSeconds,
            int maxActiveCombatBlocks,
            boolean respectProtectionPlugins) {

        public WorldProtectionSettings {
            if (combatBlockLifetimeSeconds < 0) {
                throw new IllegalArgumentException("combatBlockLifetimeSeconds cannot be negative");
            }
            if (maxActiveCombatBlocks <= 0) {
                throw new IllegalArgumentException("maxActiveCombatBlocks must be positive");
            }
        }
    }
}
