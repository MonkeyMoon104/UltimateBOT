package com.monkey.ultimatebot.utils.material;

import com.monkey.ultimatebot.common.model.PlatformCapability;
import com.monkey.ultimatebot.nms.NMSBridgeManager;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Version-safe material resolution: match → alias group peers → catalog fallback → caller
 * fallback.
 *
 * <p><b>Capability vs catalog:</b> {@link PlatformCapability} answers “can this server offer a
 * feature/mode/API?” (trim UI, mace combat kit, etc.). This catalog answers “what is the Bukkit
 * name / is the item present / what icon substitute if absent?”. Do <em>not</em> use capability to
 * decide material presence. Pre-1.16 servers lack netherite and respawn anchors: never mention
 * those {@link Material} enum fields in core (they are {@code NoSuchFieldError} at runtime).
 *
 * <p>Prefer {@link #optional(String, Material)} / {@link #stack(String, Material)} with an ancient
 * fallback ({@link Material#STONE}, {@link Material#DIAMOND_SWORD}, …) over {@link Material} enum
 * constants that may be missing at runtime. {@link #of(Material, Material)} is fragile because the
 * preferred constant is already linked at class load.
 *
 * <p>Pre-1.13 rename + durability sources: Spigot-API 1.12.2 {@code Material} javadoc (e.g.
 * {@code WATCH}, {@code TOTEM}, {@code SKULL_ITEM}, {@code STAINED_GLASS_PANE}, {@code DIODE},
 * {@code REDSTONE_COMPARATOR}).
 */
public final class MaterialCatalog {

    /**
     * Bidirectional rename groups (same item, different Bukkit name). Any member resolves by trying
     * peers until {@link Material#matchMaterial(String)} hits.
     */
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
        // Spigot 1.12.2 names (flattening renames in 1.13+).
        {"CLOCK", "WATCH"},
        {"REPEATER", "DIODE"},
        {"COMPARATOR", "REDSTONE_COMPARATOR"},
        {"TOTEM_OF_UNDYING", "TOTEM"},
        {"COBWEB", "WEB"},
        {"RAIL", "RAILS"},
        {"TNT_MINECART", "EXPLOSIVE_MINECART"},
        {"MAGMA_BLOCK", "MAGMA"},
    };

    /**
     * Substitutes for materials absent on some supported eras (added later). Not for Bukkit
     * renames (use alias groups). Netherite armor is not mapped here: the armor cycle must skip
     * the missing tier instead of showing a duplicate diamond piece.
     */
    private static final Map<String, String> FALLBACKS;

    /**
     * Legacy durability / data value when the resolved type is still a pre-flattening material
     * ({@code SKULL_ITEM}, {@code STAINED_GLASS_PANE}, …). Keyed by the <em>requested</em> modern
     * name.
     */
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
        // Heads that share SKULL_ITEM on 1.12.2 (data applied via LEGACY_DATA).
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
        // minecraft:skull / SKULL_ITEM data values (1.12.2 and below).
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

    /** Whether the named material resolves on this server (after aliases). */
    public static boolean available(String name) {
        return matchPreferred(name) != null;
    }

    /**
     * True when {@code type} is the named material on this server. Safe on versions that lack the
     * Bukkit enum field ({@code Material.RESPAWN_ANCHOR} would {@code NoSuchFieldError} on 1.15).
     */
    public static boolean is(@Nullable Material type, String name) {
        if (type == null) {
            return false;
        }
        Material expected = matchPreferred(name);
        return expected != null && type == expected;
    }

    /**
     * Structural / gameplay feature gate — not material presence. Example: {@link
     * PlatformCapability#ARMOR_TRIM} for trim selectors, not whether a helmet material exists.
     */
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

    /**
     * Resolves {@code name} via match + alias peers; if missing, uses catalog fallback then {@code
     * fallback}.
     */
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

    /** Same as {@link #optional(String, Material)} then wraps in an {@link ItemStack}. */
    public static ItemStack stack(String name, Material fallback) {
        return stack(name, fallback, 1);
    }

    /**
     * Builds an {@link ItemStack} for {@code name}, applying pre-1.13 durability when the resolved
     * type is still a shared legacy material (skull / stained glass pane).
     */
    public static ItemStack stack(String name, Material fallback, int count) {
        String key = normalize(name);
        Material type = optional(key, fallback);
        ItemStack stack = new ItemStack(type, Math.max(1, count));
        applyLegacyData(stack, key);
        return stack;
    }

    /**
     * Resolves for kits that must not fail silently when the mode is already capability-gated.
     * Still applies alias + table fallback before throwing.
     */
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

    /** Resolves a Material enum constant name without class-init on missing fields. */
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
        // Alias TOTEM should win first; GOLDEN_APPLE only if both TOTEM_OF_UNDYING and TOTEM missing.
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
