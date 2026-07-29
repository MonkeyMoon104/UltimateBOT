package com.monkey.mcbot.utils.armor;

import com.monkey.mcbot.utils.equipment.BotEquipmentUtils;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class ArmorCycle {

    private static final List<ArmorTier> ORDERED_TIERS = List.of(ArmorTier.values());

    public static Material getNextArmor(Material current, EquipmentSlot slot) {
        return getNextArmor(current, slot, ArmorTier.LEATHER, ArmorTier.NETHERITE);
    }

    public static Material getNextArmor(Material current, EquipmentSlot slot, ArmorTier minTier, ArmorTier maxTier) {
        ArmorTier resolvedMin = minTier == null ? ArmorTier.LEATHER : minTier;
        ArmorTier resolvedMax = maxTier == null ? ArmorTier.NETHERITE : maxTier;

        if (resolvedMin.compareTo(resolvedMax) > 0) {
            resolvedMin = ArmorTier.LEATHER;
            resolvedMax = ArmorTier.NETHERITE;
        }

        ArmorTier currentTier = ArmorTier.fromMaterial(current, slot);
        if (currentTier == null || currentTier.compareTo(resolvedMin) < 0 || currentTier.compareTo(resolvedMax) > 0) {
            currentTier = resolvedMin;
        }

        int minimumIndex = ORDERED_TIERS.indexOf(resolvedMin);
        int span = ORDERED_TIERS.indexOf(resolvedMax) - minimumIndex + 1;
        int relativeIndex = ORDERED_TIERS.indexOf(currentTier) - minimumIndex;
        int nextRelativeIndex = (relativeIndex + 1) % span;
        ArmorTier nextTier = ORDERED_TIERS.get(minimumIndex + nextRelativeIndex);
        return nextTier.toMaterial(slot);
    }

    public static Material clampArmor(Material current, EquipmentSlot slot, ArmorTier minTier, ArmorTier maxTier) {
        ArmorTier resolvedMin = minTier == null ? ArmorTier.LEATHER : minTier;
        ArmorTier resolvedMax = maxTier == null ? ArmorTier.NETHERITE : maxTier;

        if (resolvedMin.compareTo(resolvedMax) > 0) {
            resolvedMin = ArmorTier.LEATHER;
            resolvedMax = ArmorTier.NETHERITE;
        }

        ArmorTier currentTier = ArmorTier.fromMaterial(current, slot);
        if (currentTier == null) {
            return resolvedMin.toMaterial(slot);
        }

        if (currentTier.compareTo(resolvedMin) < 0) {
            return resolvedMin.toMaterial(slot);
        }
        if (currentTier.compareTo(resolvedMax) > 0) {
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
        String suffix =
                switch (key) {
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
