package com.monkey.mcbot.api.model;

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
    GOD
}
