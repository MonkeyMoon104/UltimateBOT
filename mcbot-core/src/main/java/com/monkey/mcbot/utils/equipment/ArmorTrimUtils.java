package com.monkey.mcbot.utils.equipment;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ArmorTrimUtils {

    private static final List<String> PATTERN_ORDER = List.of(
            "sentry", "dune", "coast", "wild", "ward", "eye", "vex", "tide",
            "snout", "rib", "spire", "wayfinder", "shaper", "silence", "raiser",
            "host", "flow", "bolt"
    );

    private static final List<String> MATERIAL_ORDER = List.of(
            "quartz", "iron", "netherite", "redstone", "copper", "gold",
            "emerald", "diamond", "lapis", "amethyst", "resin"
    );

    private ArmorTrimUtils() {
    }

    public static List<String> getTrimPatternKeys() {
        return resolveOrderedKeys(PATTERN_ORDER, trimPatternRegistry());
    }

    public static List<String> getTrimMaterialKeys() {
        return resolveOrderedKeys(MATERIAL_ORDER, trimMaterialRegistry());
    }

    public static String getNextTrimPatternKey(@Nullable String currentKey, boolean forward) {
        return getNextKey(getTrimPatternKeys(), currentKey, forward);
    }

    public static String getNextTrimMaterialKey(@Nullable String currentKey, boolean forward) {
        return getNextKey(getTrimMaterialKeys(), currentKey, forward);
    }

    public static void applyTrimSelection(Map<EquipmentSlot, ItemStack> armorMap,
                                          @Nullable String patternKey,
                                          @Nullable String materialKey) {
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

    public static void applyTrim(ItemStack item,
                                 @Nullable String patternKey,
                                 @Nullable String materialKey) {
        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        ItemMeta itemMeta = item.getItemMeta();
        if (!(itemMeta instanceof ArmorMeta armorMeta)) {
            return;
        }

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
        return resolveTrimPattern(patternKey) != null && resolveTrimMaterial(materialKey) != null;
    }

    public static boolean isTrimApplicable(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }
        return item.getItemMeta() instanceof ArmorMeta;
    }

    public static Material resolvePatternDisplayMaterial(@Nullable String patternKey) {
        return switch (normalizeKey(patternKey)) {
            case "sentry" -> materialOrDefault("SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE", Material.GUNPOWDER);
            case "dune" -> materialOrDefault("DUNE_ARMOR_TRIM_SMITHING_TEMPLATE", Material.GUNPOWDER);
            case "coast" -> materialOrDefault("COAST_ARMOR_TRIM_SMITHING_TEMPLATE", Material.GUNPOWDER);
            case "wild" -> materialOrDefault("WILD_ARMOR_TRIM_SMITHING_TEMPLATE", Material.GUNPOWDER);
            case "ward" -> materialOrDefault("WARD_ARMOR_TRIM_SMITHING_TEMPLATE", Material.GUNPOWDER);
            case "eye" -> materialOrDefault("EYE_ARMOR_TRIM_SMITHING_TEMPLATE", Material.GUNPOWDER);
            case "vex" -> materialOrDefault("VEX_ARMOR_TRIM_SMITHING_TEMPLATE", Material.GUNPOWDER);
            case "tide" -> materialOrDefault("TIDE_ARMOR_TRIM_SMITHING_TEMPLATE", Material.GUNPOWDER);
            case "snout" -> materialOrDefault("SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE", Material.GUNPOWDER);
            case "rib" -> materialOrDefault("RIB_ARMOR_TRIM_SMITHING_TEMPLATE", Material.GUNPOWDER);
            case "spire" -> materialOrDefault("SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE", Material.GUNPOWDER);
            case "wayfinder" -> materialOrDefault("WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE", Material.GUNPOWDER);
            case "shaper" -> materialOrDefault("SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE", Material.GUNPOWDER);
            case "silence" -> materialOrDefault("SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE", Material.GUNPOWDER);
            case "raiser" -> materialOrDefault("RAISER_ARMOR_TRIM_SMITHING_TEMPLATE", Material.GUNPOWDER);
            case "host" -> materialOrDefault("HOST_ARMOR_TRIM_SMITHING_TEMPLATE", Material.GUNPOWDER);
            case "flow" -> materialOrDefault("FLOW_ARMOR_TRIM_SMITHING_TEMPLATE", Material.GUNPOWDER);
            case "bolt" -> materialOrDefault("BOLT_ARMOR_TRIM_SMITHING_TEMPLATE", Material.GUNPOWDER);
            default -> Material.GUNPOWDER;
        };
    }

    public static Material resolveTrimMaterialDisplayMaterial(@Nullable String materialKey) {
        return switch (normalizeKey(materialKey)) {
            case "quartz" -> Material.QUARTZ;
            case "iron" -> Material.IRON_INGOT;
            case "netherite" -> Material.NETHERITE_INGOT;
            case "redstone" -> Material.REDSTONE;
            case "copper" -> Material.COPPER_INGOT;
            case "gold" -> Material.GOLD_INGOT;
            case "emerald" -> Material.EMERALD;
            case "diamond" -> Material.DIAMOND;
            case "lapis" -> Material.LAPIS_LAZULI;
            case "amethyst" -> materialOrDefault("AMETHYST_SHARD", Material.AMETHYST_CLUSTER);
            case "resin" -> materialOrDefault("RESIN_BRICK", Material.BRICK);
            default -> Material.IRON_INGOT;
        };
    }

    public static String formatKey(@Nullable String key) {
        String normalized = normalizeKey(key);
        if (normalized == null) {
            return "";
        }

        String[] parts = normalized.split("_");
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

    @Nullable
    private static String getNextKey(List<String> values, @Nullable String currentKey, boolean forward) {
        if (values.isEmpty()) {
            return null;
        }

        String normalizedCurrent = normalizeKey(currentKey);
        if (normalizedCurrent == null) {
            return forward ? values.getFirst() : values.getLast();
        }

        int index = values.indexOf(normalizedCurrent);
        if (index < 0) {
            return forward ? values.getFirst() : values.getLast();
        }

        int nextIndex = forward
                ? (index + 1) % values.size()
                : (index - 1 + values.size()) % values.size();
        return values.get(nextIndex);
    }

    @Nullable
    private static TrimPattern resolveTrimPattern(@Nullable String patternKey) {
        String normalized = normalizeKey(patternKey);
        return normalized == null ? null : trimPatternRegistry().get(NamespacedKey.minecraft(normalized));
    }

    @Nullable
    private static TrimMaterial resolveTrimMaterial(@Nullable String materialKey) {
        String normalized = normalizeKey(materialKey);
        return normalized == null ? null : trimMaterialRegistry().get(NamespacedKey.minecraft(normalized));
    }

    private static Registry<TrimPattern> trimPatternRegistry() {
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.TRIM_PATTERN);
    }

    private static Registry<TrimMaterial> trimMaterialRegistry() {
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.TRIM_MATERIAL);
    }

    private static <T extends org.bukkit.Keyed> List<String> resolveOrderedKeys(List<String> preferredOrder, Registry<T> registry) {
        List<String> available = new ArrayList<>();

        for (String key : preferredOrder) {
            if (registry.get(NamespacedKey.minecraft(key)) != null) {
                available.add(key);
            }
        }

        return available;
    }

    @Nullable
    private static String normalizeKey(@Nullable String key) {
        if (key == null) {
            return null;
        }

        String normalized = key.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }

    private static Material materialOrDefault(String materialName, Material fallback) {
        Material material = Material.matchMaterial(materialName);
        return material == null ? fallback : material;
    }
}
