package com.monkey.ultimatebot.config;

import java.time.Duration;
import java.util.Objects;

public final class RuntimeSettings {
    private static final CacheSettings DEFAULT_TARGET_CACHE = new CacheSettings(2_048, Duration.ofMillis(250));

    private static final CacheSettings DEFAULT_BLOCK_STATE_CACHE = new CacheSettings(16_384, Duration.ofSeconds(5));
    private static final WorldProtectionSettings DEFAULT_WORLD_PROTECTION =
            new WorldProtectionSettings(false, true, 0, 2_048, 0, 512, true);

    private final CacheSettings targetCache;
    private final CacheSettings blockStateCache;
    private final WorldProtectionSettings worldProtection;

    public RuntimeSettings(
            CacheSettings targetCache, CacheSettings blockStateCache, WorldProtectionSettings worldProtection) {
        this.targetCache = Objects.requireNonNull(targetCache, "targetCache");
        this.blockStateCache = Objects.requireNonNull(blockStateCache, "blockStateCache");
        this.worldProtection = Objects.requireNonNull(worldProtection, "worldProtection");
    }

    public static RuntimeSettings defaults() {
        return new RuntimeSettings(DEFAULT_TARGET_CACHE, DEFAULT_BLOCK_STATE_CACHE, DEFAULT_WORLD_PROTECTION);
    }

    public CacheSettings targetCache() {
        return targetCache;
    }

    public CacheSettings blockStateCache() {
        return blockStateCache;
    }

    public WorldProtectionSettings worldProtection() {
        return worldProtection;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RuntimeSettings)) {
            return false;
        }
        RuntimeSettings other = (RuntimeSettings) obj;
        return targetCache.equals(other.targetCache)
                && blockStateCache.equals(other.blockStateCache)
                && worldProtection.equals(other.worldProtection);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetCache, blockStateCache, worldProtection);
    }

    @Override
    public String toString() {
        return "RuntimeSettings[targetCache="
                + targetCache
                + ", blockStateCache="
                + blockStateCache
                + ", worldProtection="
                + worldProtection
                + "]";
    }

    public static final class CacheSettings {
        private final long maximumSize;
        private final Duration expireAfterWrite;

        public CacheSettings(long maximumSize, Duration expireAfterWrite) {
            if (maximumSize <= 0) {
                throw new IllegalArgumentException("maximumSize must be positive");
            }
            Objects.requireNonNull(expireAfterWrite, "expireAfterWrite");
            if (expireAfterWrite.isZero() || expireAfterWrite.isNegative()) {
                throw new IllegalArgumentException("expireAfterWrite must be positive");
            }
            this.maximumSize = maximumSize;
            this.expireAfterWrite = expireAfterWrite;
        }

        public long maximumSize() {
            return maximumSize;
        }

        public Duration expireAfterWrite() {
            return expireAfterWrite;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof CacheSettings)) {
                return false;
            }
            CacheSettings other = (CacheSettings) obj;
            return maximumSize == other.maximumSize && expireAfterWrite.equals(other.expireAfterWrite);
        }

        @Override
        public int hashCode() {
            return Objects.hash(maximumSize, expireAfterWrite);
        }

        @Override
        public String toString() {
            return "CacheSettings[maximumSize=" + maximumSize + ", expireAfterWrite=" + expireAfterWrite + "]";
        }
    }

    public static final class WorldProtectionSettings {
        private final boolean allowBotExplosionBlockDamage;
        private final boolean antiDupe;
        private final int combatBlockLifetimeSeconds;
        private final int maxActiveCombatBlocks;
        private final int combatEntityLifetimeSeconds;
        private final int maxActiveCombatEntities;
        private final boolean respectProtectionPlugins;

        public WorldProtectionSettings(
                boolean allowBotExplosionBlockDamage,
                boolean antiDupe,
                int combatBlockLifetimeSeconds,
                int maxActiveCombatBlocks,
                int combatEntityLifetimeSeconds,
                int maxActiveCombatEntities,
                boolean respectProtectionPlugins) {
            if (combatBlockLifetimeSeconds < 0) {
                throw new IllegalArgumentException("combatBlockLifetimeSeconds cannot be negative");
            }
            if (maxActiveCombatBlocks <= 0) {
                throw new IllegalArgumentException("maxActiveCombatBlocks must be positive");
            }
            if (combatEntityLifetimeSeconds < 0) {
                throw new IllegalArgumentException("combatEntityLifetimeSeconds cannot be negative");
            }
            if (maxActiveCombatEntities <= 0) {
                throw new IllegalArgumentException("maxActiveCombatEntities must be positive");
            }
            this.allowBotExplosionBlockDamage = allowBotExplosionBlockDamage;
            this.antiDupe = antiDupe;
            this.combatBlockLifetimeSeconds = combatBlockLifetimeSeconds;
            this.maxActiveCombatBlocks = maxActiveCombatBlocks;
            this.combatEntityLifetimeSeconds = combatEntityLifetimeSeconds;
            this.maxActiveCombatEntities = maxActiveCombatEntities;
            this.respectProtectionPlugins = respectProtectionPlugins;
        }

        public boolean allowBotExplosionBlockDamage() {
            return allowBotExplosionBlockDamage;
        }

        public boolean antiDupe() {
            return antiDupe;
        }

        public int combatBlockLifetimeSeconds() {
            return combatBlockLifetimeSeconds;
        }

        public int maxActiveCombatBlocks() {
            return maxActiveCombatBlocks;
        }

        public int combatEntityLifetimeSeconds() {
            return combatEntityLifetimeSeconds;
        }

        public int maxActiveCombatEntities() {
            return maxActiveCombatEntities;
        }

        public boolean respectProtectionPlugins() {
            return respectProtectionPlugins;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof WorldProtectionSettings)) {
                return false;
            }
            WorldProtectionSettings other = (WorldProtectionSettings) obj;
            return allowBotExplosionBlockDamage == other.allowBotExplosionBlockDamage
                    && antiDupe == other.antiDupe
                    && combatBlockLifetimeSeconds == other.combatBlockLifetimeSeconds
                    && maxActiveCombatBlocks == other.maxActiveCombatBlocks
                    && combatEntityLifetimeSeconds == other.combatEntityLifetimeSeconds
                    && maxActiveCombatEntities == other.maxActiveCombatEntities
                    && respectProtectionPlugins == other.respectProtectionPlugins;
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                    allowBotExplosionBlockDamage,
                    antiDupe,
                    combatBlockLifetimeSeconds,
                    maxActiveCombatBlocks,
                    combatEntityLifetimeSeconds,
                    maxActiveCombatEntities,
                    respectProtectionPlugins);
        }

        @Override
        public String toString() {
            return "WorldProtectionSettings[allowBotExplosionBlockDamage="
                    + allowBotExplosionBlockDamage
                    + ", antiDupe="
                    + antiDupe
                    + ", combatBlockLifetimeSeconds="
                    + combatBlockLifetimeSeconds
                    + ", maxActiveCombatBlocks="
                    + maxActiveCombatBlocks
                    + ", combatEntityLifetimeSeconds="
                    + combatEntityLifetimeSeconds
                    + ", maxActiveCombatEntities="
                    + maxActiveCombatEntities
                    + ", respectProtectionPlugins="
                    + respectProtectionPlugins
                    + "]";
        }
    }
}
