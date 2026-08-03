package com.monkey.ultimatebot.sdk.model.type;

/**
 * Type-safe difficulty values accepted by the remote API.
 */
public enum SdkDifficultyLevel {
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

    public com.monkey.ultimatebot.common.model.DifficultyTier toCommon() {
        return this == NOOB
                ? com.monkey.ultimatebot.common.model.DifficultyTier.EASY
                : com.monkey.ultimatebot.common.model.DifficultyTier.valueOf(name());
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
