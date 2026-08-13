package com.monkey.ultimatebot.utils.equipment;

import com.monkey.ultimatebot.common.model.PlatformCapability;
import com.monkey.ultimatebot.common.util.ImmutableCollections;
import com.monkey.ultimatebot.utils.material.MaterialCatalog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.jspecify.annotations.Nullable;

public final class ArmorTrimUtils {

    private static final List<String> PATTERN_ORDER = ImmutableCollections.listOf(
            "sentry",
            "dune",
            "coast",
            "wild",
            "ward",
            "eye",
            "vex",
            "tide",
            "snout",
            "rib",
            "spire",
            "wayfinder",
            "shaper",
            "silence",
            "raiser",
            "host",
            "flow",
            "bolt");

    private static final List<String> MATERIAL_ORDER = ImmutableCollections.listOf(
            "quartz",
            "iron",
            "netherite",
            "redstone",
            "copper",
            "gold",
            "emerald",
            "diamond",
            "lapis",
            "amethyst",
            "resin");

    private ArmorTrimUtils() {}

    public static List<String> getTrimPatternKeys() {
        if (!trimsEnabled()) {
            return Collections.emptyList();
        }
        return resolveOrderedKeys(PATTERN_ORDER, trimPatternRegistry());
    }

    public static List<String> getTrimMaterialKeys() {
        if (!trimsEnabled()) {
            return Collections.emptyList();
        }
        return resolveOrderedKeys(MATERIAL_ORDER, trimMaterialRegistry());
    }

    public static @Nullable String getNextTrimPatternKey(@Nullable String currentKey, boolean forward) {
        return getNextKey(getTrimPatternKeys(), currentKey, forward);
    }

    public static @Nullable String getNextTrimMaterialKey(@Nullable String currentKey, boolean forward) {
        return getNextKey(getTrimMaterialKeys(), currentKey, forward);
    }

    public static void applyTrimSelection(
            Map<EquipmentSlot, ItemStack> armorMap, @Nullable String patternKey, @Nullable String materialKey) {
        if (!trimsEnabled()) {
            return;
        }
        for (Map.Entry<EquipmentSlot, ItemStack> entry : armorMap.entrySet()) {
            ItemStack piece = entry.getValue();
            if (piece == null) {
                continue;
            }

            ItemStack updated = piece.clone();
            applyTrim(updated, patternKey, materialKey);
            entry.setValue(updated);
        }
    }

    public static void applyTrim(ItemStack item, @Nullable String patternKey, @Nullable String materialKey) {
        if (!trimsEnabled()) {
            return;
        }
        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        ItemMeta itemMeta = item.getItemMeta();
        if (!(itemMeta instanceof ArmorMeta)) {
            return;
        }
        ArmorMeta armorMeta = (ArmorMeta) itemMeta;

        TrimPattern pattern = resolveTrimPattern(patternKey);
        TrimMaterial material = resolveTrimMaterial(materialKey);

        if (pattern == null || material == null) {
            armorMeta.setTrim(null);
        } else {
            armorMeta.setTrim(new ArmorTrim(material, pattern));
        }

        item.setItemMeta(armorMeta);
    }

    public static boolean isCompleteSelection(@Nullable String patternKey, @Nullable String materialKey) {
        if (!trimsEnabled()) {
            return false;
        }
        return resolveTrimPattern(patternKey) != null && resolveTrimMaterial(materialKey) != null;
    }

    public static boolean isTrimApplicable(ItemStack item) {
        if (!trimsEnabled()) {
            return false;
        }
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }
        return item.getItemMeta() instanceof ArmorMeta;
    }

    public static Material resolvePatternDisplayMaterial(@Nullable String patternKey) {
        String key = normalizeKey(patternKey);
        final String normalized = key == null ? "" : key;
                switch (normalized) {
            case "sentry":
                return materialOrDefault("SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE", Material.GUNPOWDER);
            case "dune":
                return materialOrDefault("DUNE_ARMOR_TRIM_SMITHING_TEMPLATE", Material.GUNPOWDER);
            case "coast":
                return materialOrDefault("COAST_ARMOR_TRIM_SMITHING_TEMPLATE", Material.GUNPOWDER);
            case "wild":
                return materialOrDefault("WILD_ARMOR_TRIM_SMITHING_TEMPLATE", Material.GUNPOWDER);
            case "ward":
                return materialOrDefault("WARD_ARMOR_TRIM_SMITHING_TEMPLATE", Material.GUNPOWDER);
            case "eye":
                return materialOrDefault("EYE_ARMOR_TRIM_SMITHING_TEMPLATE", Material.GUNPOWDER);
            case "vex":
                return materialOrDefault("VEX_ARMOR_TRIM_SMITHING_TEMPLATE", Material.GUNPOWDER);
            case "tide":
                return materialOrDefault("TIDE_ARMOR_TRIM_SMITHING_TEMPLATE", Material.GUNPOWDER);
            case "snout":
                return materialOrDefault("SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE", Material.GUNPOWDER);
            case "rib":
                return materialOrDefault("RIB_ARMOR_TRIM_SMITHING_TEMPLATE", Material.GUNPOWDER);
            case "spire":
                return materialOrDefault("SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE", Material.GUNPOWDER);
            case "wayfinder":
                return materialOrDefault("WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE", Material.GUNPOWDER);
            case "shaper":
                return materialOrDefault("SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE", Material.GUNPOWDER);
            case "silence":
                return materialOrDefault("SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE", Material.GUNPOWDER);
            case "raiser":
                return materialOrDefault("RAISER_ARMOR_TRIM_SMITHING_TEMPLATE", Material.GUNPOWDER);
            case "host":
                return materialOrDefault("HOST_ARMOR_TRIM_SMITHING_TEMPLATE", Material.GUNPOWDER);
            case "flow":
                return materialOrDefault("FLOW_ARMOR_TRIM_SMITHING_TEMPLATE", Material.GUNPOWDER);
            case "bolt":
                return materialOrDefault("BOLT_ARMOR_TRIM_SMITHING_TEMPLATE", Material.GUNPOWDER);
            default:
                return Material.GUNPOWDER;
        }
    }

    public static Material resolveTrimMaterialDisplayMaterial(@Nullable String materialKey) {
        String key = normalizeKey(materialKey);
        final String normalized = key == null ? "" : key;
                switch (normalized) {
            case "quartz":
                return Material.QUARTZ;
            case "iron":
                return Material.IRON_INGOT;
            case "netherite":
                return Material.NETHERITE_INGOT;
            case "redstone":
                return Material.REDSTONE;
            case "copper":
                return Material.COPPER_INGOT;
            case "gold":
                return Material.GOLD_INGOT;
            case "emerald":
                return Material.EMERALD;
            case "diamond":
                return Material.DIAMOND;
            case "lapis":
                return Material.LAPIS_LAZULI;
            case "amethyst":
                return materialOrDefault("AMETHYST_SHARD", Material.AMETHYST_CLUSTER);
            case "resin":
                return materialOrDefault("RESIN_BRICK", Material.BRICK);
            default:
                return Material.IRON_INGOT;
        }
    }

    public static String formatKey(@Nullable String key) {
        String normalized = normalizeKey(key);
        if (normalized == null) {
            return "";
        }

        String[] parts = normalized.split("_", -1);
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return builder.toString();
    }

    private static @Nullable String getNextKey(List<String> values, @Nullable String currentKey, boolean forward) {
        if (values.isEmpty()) {
            return null;
        }

        String normalizedCurrent = normalizeKey(currentKey);
        if (normalizedCurrent == null) {
            return forward ? values.get(0) : values.get(values.size() - 1);
        }

        int index = values.indexOf(normalizedCurrent);
        if (index < 0) {
            return forward ? values.get(0) : values.get(values.size() - 1);
        }

        int nextIndex = forward ? (index + 1) % values.size() : (index - 1 + values.size()) % values.size();
        return values.get(nextIndex);
    }

    private static @Nullable TrimPattern resolveTrimPattern(@Nullable String patternKey) {
        String normalized = normalizeKey(patternKey);
        return normalized == null ? null : trimPatternRegistry().get(NamespacedKey.minecraft(normalized));
    }

    private static @Nullable TrimMaterial resolveTrimMaterial(@Nullable String materialKey) {
        String normalized = normalizeKey(materialKey);
        return normalized == null ? null : trimMaterialRegistry().get(NamespacedKey.minecraft(normalized));
    }

    private static Registry<TrimPattern> trimPatternRegistry() {
        return TrimRegistriesLookup.get().patterns();
    }

    private static Registry<TrimMaterial> trimMaterialRegistry() {
        return TrimRegistriesLookup.get().materials();
    }

    private static <T extends org.bukkit.Keyed> List<String> resolveOrderedKeys(
            List<String> preferredOrder, Registry<T> registry) {
        List<String> available = new ArrayList<>();

        for (String key : preferredOrder) {
            if (registry.get(NamespacedKey.minecraft(key)) != null) {
                available.add(key);
            }
        }

        return available;
    }

    private static @Nullable String normalizeKey(@Nullable String key) {
        if (key == null) {
            return null;
        }

        String normalized = key.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }

    private static boolean trimsEnabled() {
        return MaterialCatalog.feature(PlatformCapability.ARMOR_TRIM);
    }

    private static Material materialOrDefault(String materialName, Material fallback) {
        return com.monkey.ultimatebot.utils.material.MaterialCatalog.optional(materialName, fallback);
    }
}
