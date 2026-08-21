package com.monkey.ultimatebot.utils.material;

import com.monkey.ultimatebot.common.model.platform.PlatformCapability;
import com.monkey.ultimatebot.nms.NMSBridgeManager;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

public final class MaterialCatalog {

    private static final String[][] ALIAS_GROUPS = {
        {"GRASS", "SHORT_GRASS"},
        {"SCUTE", "TURTLE_SCUTE"},
        {"SIGN", "OAK_SIGN"},
        {"WALL_SIGN", "OAK_WALL_SIGN"},
        {"GRASS_PATH", "DIRT_PATH"},
        {"SMOOTH_STONE_BRICKS", "STONE_BRICKS"},
        {"PLAYER_HEAD", "SKULL_ITEM"},
        {"LEAD", "LEASH"},
        {"GOLDEN_SWORD", "GOLD_SWORD"},
        {"GOLDEN_PICKAXE", "GOLD_PICKAXE"},
        {"GOLDEN_AXE", "GOLD_AXE"},
        {"GOLDEN_SHOVEL", "GOLD_SPADE"},
        {"GOLDEN_HOE", "GOLD_HOE"},
        {"GOLDEN_HELMET", "GOLD_HELMET"},
        {"GOLDEN_CHESTPLATE", "GOLD_CHESTPLATE"},
        {"GOLDEN_LEGGINGS", "GOLD_LEGGINGS"},
        {"GOLDEN_BOOTS", "GOLD_BOOTS"},
        {"CLOCK", "WATCH"},
        {"REPEATER", "DIODE"},
        {"COMPARATOR", "REDSTONE_COMPARATOR"},
        {"TOTEM_OF_UNDYING", "TOTEM"},
        {"COBWEB", "WEB"},
        {"RAIL", "RAILS"},
        {"TNT_MINECART", "EXPLOSIVE_MINECART"},
        {"MAGMA_BLOCK", "MAGMA"},
    };

    private static final Map<String, String> FALLBACKS;

    private static final Map<String, Short> LEGACY_DATA;

    private static final Map<String, Short> STAINED_PANE_COLORS;

    static {
        Map<String, String> fallbacks = new HashMap<String, String>();
        fallbacks.put("MACE", "DIAMOND_SWORD");
        fallbacks.put("CROSSBOW", "BOW");
        fallbacks.put("WIND_CHARGE", "SNOWBALL");
        fallbacks.put("RECOVERY_COMPASS", "COMPASS");
        fallbacks.put("PIGLIN_HEAD", "ZOMBIE_HEAD");
        fallbacks.put("RESIN_BRICK", "BRICK");
        fallbacks.put("BARRIER", "REDSTONE_BLOCK");

        fallbacks.put("ZOMBIE_HEAD", "SKULL_ITEM");
        fallbacks.put("CREEPER_HEAD", "SKULL_ITEM");
        fallbacks.put("SKELETON_SKULL", "SKULL_ITEM");
        fallbacks.put("WITHER_SKELETON_SKULL", "SKULL_ITEM");
        fallbacks.put("DRAGON_HEAD", "SKULL_ITEM");
        FALLBACKS = Collections.unmodifiableMap(fallbacks);

        Map<String, Short> paneColors = new HashMap<String, Short>();
        paneColors.put("WHITE_STAINED_GLASS_PANE", (short) 0);
        paneColors.put("ORANGE_STAINED_GLASS_PANE", (short) 1);
        paneColors.put("MAGENTA_STAINED_GLASS_PANE", (short) 2);
        paneColors.put("LIGHT_BLUE_STAINED_GLASS_PANE", (short) 3);
        paneColors.put("YELLOW_STAINED_GLASS_PANE", (short) 4);
        paneColors.put("LIME_STAINED_GLASS_PANE", (short) 5);
        paneColors.put("PINK_STAINED_GLASS_PANE", (short) 6);
        paneColors.put("GRAY_STAINED_GLASS_PANE", (short) 7);
        paneColors.put("LIGHT_GRAY_STAINED_GLASS_PANE", (short) 8);
        paneColors.put("CYAN_STAINED_GLASS_PANE", (short) 9);
        paneColors.put("PURPLE_STAINED_GLASS_PANE", (short) 10);
        paneColors.put("BLUE_STAINED_GLASS_PANE", (short) 11);
        paneColors.put("BROWN_STAINED_GLASS_PANE", (short) 12);
        paneColors.put("GREEN_STAINED_GLASS_PANE", (short) 13);
        paneColors.put("RED_STAINED_GLASS_PANE", (short) 14);
        paneColors.put("BLACK_STAINED_GLASS_PANE", (short) 15);
        STAINED_PANE_COLORS = Collections.unmodifiableMap(paneColors);

        Map<String, Short> legacyData = new HashMap<String, Short>();
        legacyData.putAll(paneColors);

        legacyData.put("SKELETON_SKULL", (short) 0);
        legacyData.put("WITHER_SKELETON_SKULL", (short) 1);
        legacyData.put("ZOMBIE_HEAD", (short) 2);
        legacyData.put("PLAYER_HEAD", (short) 3);
        legacyData.put("CREEPER_HEAD", (short) 4);
        legacyData.put("DRAGON_HEAD", (short) 5);
        LEGACY_DATA = Collections.unmodifiableMap(legacyData);
    }

    private static final String TRIM_TEMPLATE_FALLBACK = "GUNPOWDER";
    private static final String TRIM_TEMPLATE_SUFFIX = "_ARMOR_TRIM_SMITHING_TEMPLATE";
    private static final String STAINED_GLASS_PANE_SUFFIX = "_STAINED_GLASS_PANE";

    private MaterialCatalog() {}

    public static boolean available(String name) {
        return matchPreferred(name) != null;
    }

    public static boolean is(@Nullable Material type, String name) {
        if (type == null) {
            return false;
        }
        Material expected = matchPreferred(name);
        return expected != null && type == expected;
    }

    public static boolean feature(PlatformCapability capability) {
        Objects.requireNonNull(capability, "capability");
        if (capability == PlatformCapability.ARMOR_TRIM && !armorTrimApiPresent()) {
            return false;
        }
        return NMSBridgeManager.supportsOrFalse(capability);
    }

    private static boolean armorTrimApiPresent() {
        try {
            Class.forName("org.bukkit.inventory.meta.trim.ArmorTrim");
            return true;
        } catch (ClassNotFoundException exception) {
            return false;
        }
    }

    public static Material optional(String name, Material fallback) {
        Material matched = matchPreferred(name);
        if (matched != null) {
            return matched;
        }
        String mapped = catalogFallback(normalize(name));
        if (mapped != null) {
            Material fromTable = matchPreferred(mapped);
            if (fromTable != null) {
                return fromTable;
            }
        }
        return Objects.requireNonNull(fallback, "fallback");
    }

    public static ItemStack stack(String name, Material fallback) {
        return stack(name, fallback, 1);
    }

    public static ItemStack stack(String name, Material fallback, int count) {
        String key = normalize(name);
        Material type = optional(key, fallback);
        ItemStack stack = new ItemStack(type, Math.max(1, count));
        applyLegacyData(stack, key);
        return stack;
    }

    public static Material require(String name) {
        Material matched = matchPreferred(name);
        if (matched != null) {
            return matched;
        }
        String mapped = catalogFallback(normalize(name));
        if (mapped != null) {
            Material fromTable = matchPreferred(mapped);
            if (fromTable != null) {
                return fromTable;
            }
        }
        throw new IllegalArgumentException("Unknown material: " + name);
    }

    public static Material of(Material preferred, Material fallback) {
        Objects.requireNonNull(preferred, "preferred");
        Objects.requireNonNull(fallback, "fallback");
        Material matched = Material.matchMaterial(preferred.name());
        return matched != null ? matched : fallback;
    }

    public static Material ofName(@Nullable String name, Material fallback) {
        if (name == null || name.trim().isEmpty()) {
            return Objects.requireNonNull(fallback, "fallback");
        }
        return optional(name, fallback);
    }

    private static void applyLegacyData(ItemStack stack, String requestedName) {
        Short data = LEGACY_DATA.get(requestedName);
        if (data == null) {
            return;
        }
        String typeName = stack.getType().name();
        if ("SKULL_ITEM".equals(typeName) || "STAINED_GLASS_PANE".equals(typeName)) {
            stack.setDurability(data.shortValue());
        }
    }

    private static @Nullable Material matchPreferred(String name) {
        String key = normalize(name);
        if (key.isEmpty()) {
            return null;
        }
        Material direct = Material.matchMaterial(key);
        if (direct != null) {
            return direct;
        }
        for (String[] group : ALIAS_GROUPS) {
            if (!contains(group, key)) {
                continue;
            }
            for (String peer : group) {
                if (peer.equals(key)) {
                    continue;
                }
                Material aliased = Material.matchMaterial(peer);
                if (aliased != null) {
                    return aliased;
                }
            }
        }
        return null;
    }

    private static @Nullable String catalogFallback(String key) {
        String mapped = FALLBACKS.get(key);
        if (mapped != null) {
            return mapped;
        }
        if (STAINED_PANE_COLORS.containsKey(key)
                || (key.endsWith(STAINED_GLASS_PANE_SUFFIX) && !key.equals("STAINED_GLASS_PANE"))) {
            return "STAINED_GLASS_PANE";
        }

        if (key.equals("TOTEM_OF_UNDYING")) {
            return "GOLDEN_APPLE";
        }
        if (key.endsWith(TRIM_TEMPLATE_SUFFIX)) {
            return TRIM_TEMPLATE_FALLBACK;
        }
        return null;
    }

    private static boolean contains(String[] group, String key) {
        for (String member : group) {
            if (member.equals(key)) {
                return true;
            }
        }
        return false;
    }

    private static String normalize(String name) {
        return Objects.requireNonNull(name, "name").trim().toUpperCase(Locale.ROOT);
    }
}
