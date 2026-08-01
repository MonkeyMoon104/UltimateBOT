package com.monkey.mcbot.api.model;

/** Determines how MinecraftBot manages one equipment slot. */
public enum BotEquipmentSlotMode {
    /** Keeps MinecraftBot's normal equipment and AI behavior for the slot. */
    DEFAULT,
    /** Keeps the configured item in the slot. */
    ITEM,
    /** Keeps the slot empty. */
    EMPTY
}
