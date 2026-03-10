package com.monkey.mcbot.utils.armor;

import com.monkey.mcbot.utils.equipment.BotEquipmentUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.EnumMap;
import java.util.Map;

public class ArmorCycle {

    public static Material getNextArmor(Material current, EquipmentSlot slot) {
        return getNextArmor(current, slot, ArmorTier.LEATHER, ArmorTier.NETHERITE);
    }

    public static Material getNextArmor(Material current, EquipmentSlot slot, ArmorTier minTier, ArmorTier maxTier) {
        ArmorTier resolvedMin = minTier == null ? ArmorTier.LEATHER : minTier;
        ArmorTier resolvedMax = maxTier == null ? ArmorTier.NETHERITE : maxTier;

        if (resolvedMin.ordinal() > resolvedMax.ordinal()) {
            resolvedMin = ArmorTier.LEATHER;
            resolvedMax = ArmorTier.NETHERITE;
        }

        ArmorTier currentTier = ArmorTier.fromMaterial(current, slot);
        if (currentTier == null || currentTier.ordinal() < resolvedMin.ordinal() || currentTier.ordinal() > resolvedMax.ordinal()) {
            currentTier = resolvedMin;
        }

        int span = resolvedMax.ordinal() - resolvedMin.ordinal() + 1;
        int relativeIndex = currentTier.ordinal() - resolvedMin.ordinal();
        int nextRelativeIndex = (relativeIndex + 1) % span;
        ArmorTier nextTier = ArmorTier.values()[resolvedMin.ordinal() + nextRelativeIndex];
        return nextTier.toMaterial(slot);
    }

    public static Material clampArmor(Material current, EquipmentSlot slot, ArmorTier minTier, ArmorTier maxTier) {
        ArmorTier resolvedMin = minTier == null ? ArmorTier.LEATHER : minTier;
        ArmorTier resolvedMax = maxTier == null ? ArmorTier.NETHERITE : maxTier;

        if (resolvedMin.ordinal() > resolvedMax.ordinal()) {
            resolvedMin = ArmorTier.LEATHER;
            resolvedMax = ArmorTier.NETHERITE;
        }

        ArmorTier currentTier = ArmorTier.fromMaterial(current, slot);
        if (currentTier == null) {
            return resolvedMin.toMaterial(slot);
        }

        if (currentTier.ordinal() < resolvedMin.ordinal()) {
            return resolvedMin.toMaterial(slot);
        }
        if (currentTier.ordinal() > resolvedMax.ordinal()) {
            return resolvedMax.toMaterial(slot);
        }

        return currentTier.toMaterial(slot);
    }

    public static Map<EquipmentSlot, ItemStack> getDefaultArmorFromConfig(FileConfiguration config, Plugin plugin) {
        Map<EquipmentSlot, ItemStack> defaultArmor = new EnumMap<>(EquipmentSlot.class);

        ItemStack helmet = new ItemStack(getMaterialFromConfig(config, "helmet", plugin));
        BotEquipmentUtils.applyArmorEnchants(helmet, false);
        defaultArmor.put(EquipmentSlot.HEAD, helmet);

        ItemStack chestplate = new ItemStack(getMaterialFromConfig(config, "chestplate", plugin));
        BotEquipmentUtils.applyArmorEnchants(chestplate, false);
        defaultArmor.put(EquipmentSlot.CHEST, chestplate);

        ItemStack leggings = new ItemStack(getMaterialFromConfig(config, "leggings", plugin));
        BotEquipmentUtils.applyArmorEnchants(leggings, false);
        defaultArmor.put(EquipmentSlot.LEGS, leggings);

        ItemStack boots = new ItemStack(getMaterialFromConfig(config, "boots", plugin));
        BotEquipmentUtils.applyArmorEnchants(boots, false);
        defaultArmor.put(EquipmentSlot.FEET, boots);

        return defaultArmor;
    }


    public static Material getMaterialFromConfig(FileConfiguration config, String key, Plugin plugin) {
        String matName = config.getString("gui.default-armor." + key, "NETHERITE");
        String suffix = switch (key) {
            case "helmet" -> "_HELMET";
            case "chestplate" -> "_CHESTPLATE";
            case "leggings" -> "_LEGGINGS";
            case "boots" -> "_BOOTS";
            default -> "";
        };

        try {
            return Material.valueOf(matName + suffix);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Materiale armatura non valido (" + matName + suffix + "): " + e);
            return Material.AIR;
        }
    }
}
