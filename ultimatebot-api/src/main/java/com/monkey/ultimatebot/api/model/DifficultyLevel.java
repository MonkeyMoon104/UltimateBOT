package com.monkey.ultimatebot.api.model;

/**
 * Bot difficulty/behavior difficulties from easiest to most advanced.
 *
 * <p>Order is meaningful for min/max range validation in {@link BotSettings}.</p>
 */
public enum DifficultyLevel {
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

    public com.monkey.ultimatebot.common.model.DifficultyTier toCommon() {
        return com.monkey.ultimatebot.common.model.DifficultyTier.valueOf(name());
    }

    public static DifficultyLevel fromCommon(com.monkey.ultimatebot.common.model.DifficultyTier difficultyTier) {
        return valueOf(java.util.Objects.requireNonNull(difficultyTier, "difficultyTier")
                .name());
    }
}
