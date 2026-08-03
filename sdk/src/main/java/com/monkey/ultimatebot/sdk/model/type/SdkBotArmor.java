package com.monkey.ultimatebot.sdk.model.type;

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

    public com.monkey.ultimatebot.common.model.BotArmorTier toCommon() {
        return this == GOLD
                ? com.monkey.ultimatebot.common.model.BotArmorTier.GOLDEN
                : com.monkey.ultimatebot.common.model.BotArmorTier.valueOf(name());
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
