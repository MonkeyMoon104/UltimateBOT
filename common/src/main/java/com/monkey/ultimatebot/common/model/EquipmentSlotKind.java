package com.monkey.ultimatebot.common.model;

/**
 * Version-neutral equipment slots.
 *
 * <p>{@code org.bukkit.inventory.EquipmentSlot} does not exist on Spigot 1.7.10. Shared API and NMS
 * bridges use this enum so 1.7 never links the Bukkit type.
 */
public enum EquipmentSlotKind {
    HAND,
    OFF_HAND,
    FEET,
    LEGS,
    CHEST,
    HEAD
}
