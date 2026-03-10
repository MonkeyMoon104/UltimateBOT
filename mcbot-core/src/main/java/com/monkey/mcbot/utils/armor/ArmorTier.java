package com.monkey.mcbot.utils.armor;

import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;

public enum ArmorTier {
    LEATHER,
    IRON,
    GOLDEN,
    DIAMOND,
    NETHERITE;

    public Material toMaterial(EquipmentSlot slot) {
        String suffix = suffixFor(slot);
        if (suffix.isEmpty()) {
            return Material.AIR;
        }
        return Material.valueOf(name() + suffix);
    }

    public static ArmorTier fromMaterial(Material material, EquipmentSlot slot) {
        if (material == null) {
            return null;
        }

        String suffix = suffixFor(slot);
        if (suffix.isEmpty()) {
            return null;
        }

        String materialName = material.name();
        if (!materialName.endsWith(suffix)) {
            return null;
        }

        String baseName = materialName.substring(0, materialName.length() - suffix.length());
        try {
            return ArmorTier.valueOf(baseName);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static String suffixFor(EquipmentSlot slot) {
        if (slot == null) {
            return "";
        }
        return switch (slot) {
            case HEAD -> "_HELMET";
            case CHEST -> "_CHESTPLATE";
            case LEGS -> "_LEGGINGS";
            case FEET -> "_BOOTS";
            default -> "";
        };
    }
}
