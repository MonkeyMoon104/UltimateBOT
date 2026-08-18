package com.monkey.ultimatebot.utils.armor;

import com.monkey.ultimatebot.common.model.EquipmentSlotKind;
import com.monkey.ultimatebot.utils.equipment.BotEquipmentUtils;
import com.monkey.ultimatebot.utils.material.MaterialCatalog;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.Nullable;

public class ArmorCycle {

    private static final List<ArmorTier> ORDERED_TIERS =
            java.util.Collections.unmodifiableList(java.util.Arrays.asList(ArmorTier.values()));

    public static Material getNextArmor(Material current, EquipmentSlotKind slot) {
        return getNextArmor(current, slot, ArmorTier.LEATHER, ArmorTier.maxAvailable());
    }

    public static Material getNextArmor(
            Material current, EquipmentSlotKind slot, ArmorTier minTier, ArmorTier maxTier) {
        ArmorTier resolvedMin = clampMin(minTier, maxTier);
        ArmorTier resolvedMax = clampMax(maxTier);

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
        for (int step = 1; step <= span; step++) {
            int nextRelativeIndex = (relativeIndex + step) % span;
            ArmorTier nextTier = ORDERED_TIERS.get(minimumIndex + nextRelativeIndex);
            Material material = nextTier.toMaterial(slot);
            if (material != Material.AIR) {
                return material;
            }
        }
        return resolvedMin.toMaterial(slot);
    }

    public static Material clampArmor(Material current, EquipmentSlotKind slot, ArmorTier minTier, ArmorTier maxTier) {
        ArmorTier resolvedMin = clampMin(minTier, maxTier);
        ArmorTier resolvedMax = clampMax(maxTier);

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

        Material material = currentTier.toMaterial(slot);
        return material == Material.AIR ? resolvedMax.toMaterial(slot) : material;
    }

    public static Map<EquipmentSlotKind, ItemStack> getDefaultArmorFromConfig(FileConfiguration config, Plugin plugin) {
        Map<EquipmentSlotKind, ItemStack> defaultArmor = new EnumMap<>(EquipmentSlotKind.class);

        ItemStack helmet = new ItemStack(getMaterialFromConfig(config, "helmet", plugin));
        BotEquipmentUtils.applyArmorEnchants(helmet, false);
        defaultArmor.put(EquipmentSlotKind.HEAD, helmet);

        ItemStack chestplate = new ItemStack(getMaterialFromConfig(config, "chestplate", plugin));
        BotEquipmentUtils.applyArmorEnchants(chestplate, false);
        defaultArmor.put(EquipmentSlotKind.CHEST, chestplate);

        ItemStack leggings = new ItemStack(getMaterialFromConfig(config, "leggings", plugin));
        BotEquipmentUtils.applyArmorEnchants(leggings, false);
        defaultArmor.put(EquipmentSlotKind.LEGS, leggings);

        ItemStack boots = new ItemStack(getMaterialFromConfig(config, "boots", plugin));
        BotEquipmentUtils.applyArmorEnchants(boots, false);
        defaultArmor.put(EquipmentSlotKind.FEET, boots);

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

        EquipmentSlotKind slot = slotForKey(key);
        ArmorTier requested = parseTierName(matName);
        if (requested != null) {
            if (requested.compareTo(ArmorTier.maxAvailable()) > 0) {
                return ArmorTier.maxAvailable().toMaterial(slot);
            }
            Material fromTier = requested.toMaterial(slot);
            if (fromTier != Material.AIR) {
                return fromTier;
            }
        }

        String fullName = matName + suffix;
        Material material = MaterialCatalog.optional(fullName, Material.AIR);
        if (material == Material.AIR) {
            plugin.getLogger().warning("Materiale armatura non valido (" + fullName + ")");
            return ArmorTier.maxAvailable().toMaterial(slot);
        }
        ArmorTier tier = ArmorTier.fromMaterial(material, slot);
        if (tier != null && tier.compareTo(ArmorTier.maxAvailable()) > 0) {
            return ArmorTier.maxAvailable().toMaterial(slot);
        }
        return material;
    }

    private static ArmorTier clampMax(@Nullable ArmorTier maxTier) {
        ArmorTier platformMax = ArmorTier.maxAvailable();
        if (maxTier == null) {
            return platformMax;
        }
        return maxTier.compareTo(platformMax) > 0 ? platformMax : maxTier;
    }

    private static ArmorTier clampMin(@Nullable ArmorTier minTier, @Nullable ArmorTier maxTier) {
        ArmorTier resolvedMin = minTier == null ? ArmorTier.LEATHER : minTier;
        ArmorTier resolvedMax = clampMax(maxTier);
        return resolvedMin.compareTo(resolvedMax) > 0 ? ArmorTier.LEATHER : resolvedMin;
    }

    private static EquipmentSlotKind slotForKey(String key) {
        switch (key) {
            case "helmet":
                return EquipmentSlotKind.HEAD;
            case "chestplate":
                return EquipmentSlotKind.CHEST;
            case "leggings":
                return EquipmentSlotKind.LEGS;
            case "boots":
                return EquipmentSlotKind.FEET;
            default:
                return EquipmentSlotKind.HEAD;
        }
    }

    private static @Nullable ArmorTier parseTierName(@Nullable String matName) {
        if (matName == null || matName.trim().isEmpty()) {
            return null;
        }
        try {
            return ArmorTier.valueOf(matName.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
