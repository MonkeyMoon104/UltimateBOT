package com.monkey.ultimatebot.api.model.configuration;

/** Determines how UltimateBot manages one equipment slot. */
public enum BotEquipmentSlotMode {
    /** Keeps UltimateBot's normal equipment and AI behavior for the slot. */
    DEFAULT,
    /** Keeps the configured item in the slot. */
    ITEM,
    /** Keeps the slot empty. */
    EMPTY
}
