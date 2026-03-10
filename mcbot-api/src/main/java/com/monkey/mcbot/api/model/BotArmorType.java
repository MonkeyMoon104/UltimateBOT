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
    NETHERITE
}
