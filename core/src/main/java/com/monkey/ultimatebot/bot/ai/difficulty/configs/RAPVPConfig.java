package com.monkey.ultimatebot.bot.ai.difficulty.configs;

public class RAPVPConfig {
    private final int maxDistance;
    private final int predictionTicks;
    private final double minMovement;
    private final double minSafeDistance;
    private final long anchorSearchCooldownMillis;

    private RAPVPConfig(Builder builder) {
        this.maxDistance = builder.maxDistance;
        this.predictionTicks = builder.predictionTicks;
        this.minMovement = builder.minMovement;
        this.minSafeDistance = builder.minSafeDistance;
        this.anchorSearchCooldownMillis = builder.anchorSearchCooldownMillis;
    }

    public int getMaxDistance() {
        return maxDistance;
    }

    public int getPredictionTicks() {
        return predictionTicks;
    }

    public double getMinMovement() {
        return minMovement;
    }

    public double getMinSafeDistance() {
        return minSafeDistance;
    }

    public long getAnchorSearchCooldownMillis() {
        return anchorSearchCooldownMillis;
    }

    public static class Builder {
        private int maxDistance = 12;
        private int predictionTicks = 10;
        private double minMovement = 0.1;
        private double minSafeDistance = 4.0;
        private long anchorSearchCooldownMillis = 500L;

        public Builder maxDistance(int val) {
            this.maxDistance = val;
            return this;
        }

        public Builder predictionTicks(int val) {
            this.predictionTicks = val;
            return this;
        }

        public Builder minMovement(double val) {
            this.minMovement = val;
            return this;
        }

        public Builder minSafeDistance(double val) {
            this.minSafeDistance = val;
            return this;
        }

        public Builder anchorSearchCooldownMillis(long val) {
            this.anchorSearchCooldownMillis = val;
            return this;
        }

        public RAPVPConfig build() {
            return new RAPVPConfig(this);
        }
    }
}
