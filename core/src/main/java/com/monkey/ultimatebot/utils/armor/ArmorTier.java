package com.monkey.ultimatebot.utils.armor;

import com.monkey.ultimatebot.common.model.bot.EquipmentSlotKind;
import com.monkey.ultimatebot.common.model.platform.PlatformCapability;
import com.monkey.ultimatebot.utils.material.MaterialCatalog;
import org.bukkit.Material;
import org.jspecify.annotations.Nullable;

public enum ArmorTier {
    LEATHER,
    IRON,
    GOLDEN,
    DIAMOND,
    NETHERITE;

    public static ArmorTier maxAvailable() {
        if (com.monkey.ultimatebot.nms.NMSBridgeManager.isInitialized()
                && !com.monkey.ultimatebot.nms.NMSBridgeManager.supports(PlatformCapability.NETHERITE)) {
            return DIAMOND;
        }
        return MaterialCatalog.available("NETHERITE_HELMET") ? NETHERITE : DIAMOND;
    }

    public Material toMaterial(EquipmentSlotKind slot) {
        String suffix = suffixFor(slot);
        if (suffix.isEmpty()) {
            return Material.AIR;
        }
        return MaterialCatalog.optional(name() + suffix, Material.AIR);
    }

    public static @Nullable ArmorTier fromMaterial(@Nullable Material material, @Nullable EquipmentSlotKind slot) {
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

    private static String suffixFor(@Nullable EquipmentSlotKind slot) {
        if (slot == null) {
            return "";
        }
        switch (slot) {
            case HEAD:
                return "_HELMET";
            case CHEST:
                return "_CHESTPLATE";
            case LEGS:
                return "_LEGGINGS";
            case FEET:
                return "_BOOTS";
            default:
                return "";
        }
    }
}
