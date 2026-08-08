package com.monkey.ultimatebot.utils.equipment;

public class EquipmentConverter {

    public static org.bukkit.inventory.EquipmentSlot[] getArmorSlots() {
        return new org.bukkit.inventory.EquipmentSlot[] {
            org.bukkit.inventory.EquipmentSlot.HEAD,
            org.bukkit.inventory.EquipmentSlot.CHEST,
            org.bukkit.inventory.EquipmentSlot.LEGS,
            org.bukkit.inventory.EquipmentSlot.FEET
        };
    }
}
