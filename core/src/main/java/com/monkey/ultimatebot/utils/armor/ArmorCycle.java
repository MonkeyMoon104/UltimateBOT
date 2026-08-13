package com.monkey.ultimatebot.utils.armor;

import com.monkey.ultimatebot.utils.equipment.BotEquipmentUtils;
import com.monkey.ultimatebot.utils.material.MaterialCatalog;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class ArmorCycle {

    private static final List<ArmorTier> ORDERED_TIERS = java.util.Collections.unmodifiableList(java.util.Arrays.asList(ArmorTier.values()));

    public static Material getNextArmor(Material current, EquipmentSlot slot) {
        return getNextArmor(current, slot, ArmorTier.LEATHER, ArmorTier.maxAvailable());
    }

    public static Material getNextArmor(Material current, EquipmentSlot slot, ArmorTier minTier, ArmorTier maxTier) {
        ArmorTier resolvedMin = minTier == null ? ArmorTier.LEATHER : minTier;
        ArmorTier resolvedMax = maxTier == null ? ArmorTier.maxAvailable() : maxTier;

        if (resolvedMin.compareTo(resolvedMax) > 0) {
            resolvedMin = ArmorTier.LEATHER;
            resolvedMax = ArmorTier.maxAvailable();
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
        ArmorTier resolvedMax = maxTier == null ? ArmorTier.maxAvailable() : maxTier;

        if (resolvedMin.compareTo(resolvedMax) > 0) {
            resolvedMin = ArmorTier.LEATHER;
            resolvedMax = ArmorTier.maxAvailable();
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
        String suffix;
                                switch (key) {
                    case "helmet":
                        suffix = "_HELMET";
                        break;
                    case "chestplate":
                        suffix = "_CHESTPLATE";
                        break;
                    case "leggings":
                        suffix = "_LEGGINGS";
                        break;
                    case "boots":
                        suffix = "_BOOTS";
                        break;
                    default:
                        suffix = "";
                        break;
                }

        String fullName = matName + suffix;
        Material material = MaterialCatalog.optional(fullName, Material.AIR);
        if (material == Material.AIR) {
            plugin.getLogger().warning("Materiale armatura non valido (" + fullName + ")");
        }
        // Clamp netherite config to diamond when platform max is diamond.
        EquipmentSlot slot;
        switch (key) {
            case "helmet":
                slot = EquipmentSlot.HEAD;
                break;
            case "chestplate":
                slot = EquipmentSlot.CHEST;
                break;
            case "leggings":
                slot = EquipmentSlot.LEGS;
                break;
            case "boots":
                slot = EquipmentSlot.FEET;
                break;
            default:
                slot = null;
                break;
        }
        ArmorTier tier = ArmorTier.fromMaterial(material, slot);
        if (tier != null && tier.compareTo(ArmorTier.maxAvailable()) > 0) {
            EquipmentSlot clampSlot = slot != null ? slot : EquipmentSlot.HEAD;
            return ArmorTier.maxAvailable().toMaterial(clampSlot);
        }
        return material;
    }
}
