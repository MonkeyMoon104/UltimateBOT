package com.monkey.ultimatebot.sdk.model;

/**
 * Type-safe rank values accepted by the remote API.
 */
public enum SdkBotRank {
    /**
     * Entry-level behavior profile.
     */
    NOOB,

    /**
     * Standard baseline profile.
     */
    EASY,

    /**
     * Intermediate profile.
     */
    MEDIUM,

    /**
     * High difficulty profile.
     */
    HARD,

    /**
     * Maximum difficulty profile.
     */
    GOD;

    public com.monkey.ultimatebot.common.model.BotRankTier toCommon() {
        return this == NOOB
                ? com.monkey.ultimatebot.common.model.BotRankTier.EASY
                : com.monkey.ultimatebot.common.model.BotRankTier.valueOf(name());
    }

    /**
     * Returns the API string value.
     *
     * @return API value
     */
    public String apiValue() {
        return name();
    }
}
