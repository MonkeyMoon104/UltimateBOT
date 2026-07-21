package com.monkey.mcbot.sdk.model;

/**
 * Type-safe armor values accepted by the remote API.
 */
public enum SdkBotArmor {
    /**
     * Leather armor tier.
     */
    LEATHER,

    /**
     * Iron armor tier.
     */
    IRON,

    /**
     * Golden armor tier.
     */
    GOLD,

    /**
     * Diamond armor tier.
     */
    DIAMOND,

    /**
     * Netherite armor tier.
     */
    NETHERITE;

    /**
     * Returns the API string value.
     *
     * @return API value
     */
    public String apiValue() {
        return name();
    }
}
