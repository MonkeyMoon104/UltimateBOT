package com.monkey.ultimatebot.bot.ai.rank.configs;

public class CPVPConfig {
    private final double maxCrystalDistance;
    private final double minCrystalDistance;
    private final double crystalAttackRange;
    private final double optimalDamageRange;
    private final int obsidianPlaceCooldownTicks;
    private final int crystalPlaceCooldownTicks;
    private final int attackCooldownTicks;
    private final int obsidianPreparationTime;
    private final int crystalPreparationTime;
    private final int attackPreparationTime;
    private final long positionCooldownMs;
    private final long obsidianCacheMs;
    private final long fullScanIntervalMs;
    private final long positionCacheMs;
    private final int maxCrystalsPerPosition;
    private final int maxPositionsToCheck;
    private final double minCrystalScore;
    private final double minAttackScore;

    private CPVPConfig(Builder builder) {
        this.maxCrystalDistance = builder.maxCrystalDistance;
        this.minCrystalDistance = builder.minCrystalDistance;
        this.crystalAttackRange = builder.crystalAttackRange;
        this.optimalDamageRange = builder.optimalDamageRange;
        this.obsidianPlaceCooldownTicks = builder.obsidianPlaceCooldownTicks;
        this.crystalPlaceCooldownTicks = builder.crystalPlaceCooldownTicks;
        this.attackCooldownTicks = builder.attackCooldownTicks;
        this.obsidianPreparationTime = builder.obsidianPreparationTime;
        this.crystalPreparationTime = builder.crystalPreparationTime;
        this.attackPreparationTime = builder.attackPreparationTime;
        this.positionCooldownMs = builder.positionCooldownMs;
        this.obsidianCacheMs = builder.obsidianCacheMs;
        this.fullScanIntervalMs = builder.fullScanIntervalMs;
        this.positionCacheMs = builder.positionCacheMs;
        this.maxCrystalsPerPosition = builder.maxCrystalsPerPosition;
        this.maxPositionsToCheck = builder.maxPositionsToCheck;
        this.minCrystalScore = builder.minCrystalScore;
        this.minAttackScore = builder.minAttackScore;
    }

    public double getMaxCrystalDistance() {
        return maxCrystalDistance;
    }

    public double getMinCrystalDistance() {
        return minCrystalDistance;
    }

    public double getCrystalAttackRange() {
        return crystalAttackRange;
    }

    public double getOptimalDamageRange() {
        return optimalDamageRange;
    }

    public int getObsidianPlaceCooldownTicks() {
        return obsidianPlaceCooldownTicks;
    }

    public int getCrystalPlaceCooldownTicks() {
        return crystalPlaceCooldownTicks;
    }

    public int getAttackCooldownTicks() {
        return attackCooldownTicks;
    }

    public int getObsidianPreparationTime() {
        return obsidianPreparationTime;
    }

    public int getCrystalPreparationTime() {
        return crystalPreparationTime;
    }

    public int getAttackPreparationTime() {
        return attackPreparationTime;
    }

    public long getPositionCooldownMs() {
        return positionCooldownMs;
    }

    public long getObsidianCacheMs() {
        return obsidianCacheMs;
    }

    public long getFullScanIntervalMs() {
        return fullScanIntervalMs;
    }

    public long getPositionCacheMs() {
        return positionCacheMs;
    }

    public int getMaxCrystalsPerPosition() {
        return maxCrystalsPerPosition;
    }

    public int getMaxPositionsToCheck() {
        return maxPositionsToCheck;
    }

    public double getMinCrystalScore() {
        return minCrystalScore;
    }

    public double getMinAttackScore() {
        return minAttackScore;
    }

    public static class Builder {
        private double maxCrystalDistance = 10.0;
        private double minCrystalDistance = 2.5;
        private double crystalAttackRange = 8.0;
        private double optimalDamageRange = 6.0;
        private int obsidianPlaceCooldownTicks = 4;
        private int crystalPlaceCooldownTicks = 5;
        private int attackCooldownTicks = 3;
        private int obsidianPreparationTime = 2;
        private int crystalPreparationTime = 2;
        private int attackPreparationTime = 1;
        private long positionCooldownMs = 1500L;
        private long obsidianCacheMs = 6000L;
        private long fullScanIntervalMs = 500L;
        private long positionCacheMs = 1000L;
        private int maxCrystalsPerPosition = 7;
        private int maxPositionsToCheck = 3;
        private double minCrystalScore = 5.0;
        private double minAttackScore = 0.3;

        public Builder maxCrystalDistance(double val) {
            this.maxCrystalDistance = val;
            return this;
        }

        public Builder minCrystalDistance(double val) {
            this.minCrystalDistance = val;
            return this;
        }

        public Builder crystalAttackRange(double val) {
            this.crystalAttackRange = val;
            return this;
        }

        public Builder optimalDamageRange(double val) {
            this.optimalDamageRange = val;
            return this;
        }

        public Builder obsidianPlaceCooldownTicks(int val) {
            this.obsidianPlaceCooldownTicks = val;
            return this;
        }

        public Builder crystalPlaceCooldownTicks(int val) {
            this.crystalPlaceCooldownTicks = val;
            return this;
        }

        public Builder attackCooldownTicks(int val) {
            this.attackCooldownTicks = val;
            return this;
        }

        public Builder obsidianPreparationTime(int val) {
            this.obsidianPreparationTime = val;
            return this;
        }

        public Builder crystalPreparationTime(int val) {
            this.crystalPreparationTime = val;
            return this;
        }

        public Builder attackPreparationTime(int val) {
            this.attackPreparationTime = val;
            return this;
        }

        public Builder positionCooldownMs(long val) {
            this.positionCooldownMs = val;
            return this;
        }

        public Builder obsidianCacheMs(long val) {
            this.obsidianCacheMs = val;
            return this;
        }

        public Builder fullScanIntervalMs(long val) {
            this.fullScanIntervalMs = val;
            return this;
        }

        public Builder positionCacheMs(long val) {
            this.positionCacheMs = val;
            return this;
        }

        public Builder maxCrystalsPerPosition(int val) {
            this.maxCrystalsPerPosition = val;
            return this;
        }

        public Builder maxPositionsToCheck(int val) {
            this.maxPositionsToCheck = val;
            return this;
        }

        public Builder minCrystalScore(double val) {
            this.minCrystalScore = val;
            return this;
        }

        public Builder minAttackScore(double val) {
            this.minAttackScore = val;
            return this;
        }

        public CPVPConfig build() {
            return new CPVPConfig(this);
        }
    }
}
