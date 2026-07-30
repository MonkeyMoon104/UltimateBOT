package com.monkey.mcbot.api.model;

/**
 * Armor tiers available for bot equipment configuration.
 *
 * <p>Values are ordered by progression and are used for min/max range checks
 * inside {@link BotSettings}.</p>
 */
public enum BotArmorType {
    /** Leather armor tier. */
    LEATHER,
    /** Iron armor tier. */
    IRON,
    /** Golden armor tier. */
    GOLDEN,
    /** Diamond armor tier. */
    DIAMOND,
    /** Netherite armor tier. */
    NETHERITE;

    public com.monkey.mcbot.common.model.BotArmorTier toCommon() {
        return com.monkey.mcbot.common.model.BotArmorTier.valueOf(name());
    }

    public static BotArmorType fromCommon(com.monkey.mcbot.common.model.BotArmorTier armorTier) {
        return valueOf(java.util.Objects.requireNonNull(armorTier, "armorTier").name());
    }
}
