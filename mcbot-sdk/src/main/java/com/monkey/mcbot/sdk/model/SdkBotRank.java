package com.monkey.mcbot.sdk.model;

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

    /**
     * Returns the API string value.
     *
     * @return API value
     */
    public String apiValue() {
        return name();
    }
}
