package com.monkey.mcbot.api.model;

import java.util.Objects;

public final class BotSettings {

    private final boolean follow;
    private final boolean combat;
    private final int totemCount;
    private final int minTotemCount;
    private final int maxTotemCount;
    private final BotRank rank;
    private final BotRank minRank;
    private final BotRank maxRank;

    private BotSettings(Builder builder) {
        this.follow = builder.follow;
        this.combat = builder.combat;
        this.totemCount = builder.totemCount;
        this.minTotemCount = builder.minTotemCount;
        this.maxTotemCount = builder.maxTotemCount;
        this.rank = builder.rank;
        this.minRank = builder.minRank;
        this.maxRank = builder.maxRank;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean follow() {
        return follow;
    }

    public boolean combat() {
        return combat;
    }

    public int totemCount() {
        return totemCount;
    }

    public int minTotemCount() {
        return minTotemCount;
    }

    public int maxTotemCount() {
        return maxTotemCount;
    }

    public BotRank rank() {
        return rank;
    }

    public BotRank minRank() {
        return minRank;
    }

    public BotRank maxRank() {
        return maxRank;
    }

    public static final class Builder {
        private boolean follow = false;
        private boolean combat = false;
        private Integer totemCount = null;
        private int minTotemCount = -1;
        private int maxTotemCount = Integer.MAX_VALUE;
        private BotRank rank = null;
        private BotRank minRank = BotRank.EASY;
        private BotRank maxRank = BotRank.GOD;

        private Builder() {
        }

        public Builder follow(boolean follow) {
            this.follow = follow;
            return this;
        }

        public Builder combat(boolean combat) {
            this.combat = combat;
            return this;
        }

        public Builder totemCount(int totemCount) {
            this.totemCount = totemCount;
            return this;
        }

        /**
         * Defines the allowed totem range for this bot.
         * Use -1 as minimum to allow unlimited totems.
         */
        public Builder totemValue(int min, int max) {
            this.minTotemCount = min;
            this.maxTotemCount = max;
            return this;
        }

        public Builder rank(BotRank rank) {
            this.rank = Objects.requireNonNull(rank, "rank");
            return this;
        }

        /**
         * Defines allowed rank range for this bot.
         */
        public Builder rankValue(BotRank minRank, BotRank maxRank) {
            this.minRank = Objects.requireNonNull(minRank, "minRank");
            this.maxRank = Objects.requireNonNull(maxRank, "maxRank");
            return this;
        }

        public BotSettings build() {
            validateTotemRange(minTotemCount, maxTotemCount);
            validateRankRange(minRank, maxRank);

            if (totemCount == null) {
                totemCount = minTotemCount <= -1 ? -1 : minTotemCount;
            }

            if (totemCount < minTotemCount || totemCount > maxTotemCount) {
                if (totemCount != -1 || minTotemCount > -1) {
                    throw new IllegalArgumentException(
                            "totemCount must be within range [" + minTotemCount + ", " + maxTotemCount + "]"
                    );
                }
            }

            if (rank == null) {
                rank = minRank;
            }

            if (rank.ordinal() < minRank.ordinal() || rank.ordinal() > maxRank.ordinal()) {
                throw new IllegalArgumentException(
                        "rank must be within range [" + minRank + ", " + maxRank + "]"
                );
            }

            return new BotSettings(this);
        }

        private static void validateTotemRange(int min, int max) {
            if (min < -1) {
                throw new IllegalArgumentException("min totem cannot be less than -1");
            }
            if (max < 0) {
                throw new IllegalArgumentException("max totem must be >= 0");
            }
            if (min > max) {
                throw new IllegalArgumentException("min totem cannot be greater than max totem");
            }
        }

        private static void validateRankRange(BotRank min, BotRank max) {
            if (min.ordinal() > max.ordinal()) {
                throw new IllegalArgumentException("min rank cannot be greater than max rank");
            }
        }
    }
}
