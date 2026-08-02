package com.monkey.ultimatebot.api.model;

/**
 * Bot difficulty/behavior ranks from easiest to most advanced.
 *
 * <p>Order is meaningful for min/max range validation in {@link BotSettings}.</p>
 */
public enum BotRank {
    /** Entry-level behavior profile. */
    EASY,
    /** Standard baseline profile. */
    NORMAL,
    /** Intermediate profile. */
    MEDIUM,
    /** High difficulty profile. */
    HARD,
    /** Maximum difficulty profile. */
    GOD;

    public com.monkey.ultimatebot.common.model.BotRankTier toCommon() {
        return com.monkey.ultimatebot.common.model.BotRankTier.valueOf(name());
    }

    public static BotRank fromCommon(com.monkey.ultimatebot.common.model.BotRankTier rankTier) {
        return valueOf(java.util.Objects.requireNonNull(rankTier, "rankTier").name());
    }
}
