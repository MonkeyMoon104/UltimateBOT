package com.monkey.ultimatebot.utils.material;

import com.monkey.ultimatebot.common.model.PlatformCapability;
import com.monkey.ultimatebot.nms.NMSBridgeManager;
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
 * decide material presence for items that exist across the whole 1.17+ support range (totem,
 * end crystal, respawn anchor, netherite, …).
 *
 * <p>Prefer {@link #optional(String, Material)} with an ancient fallback ({@link Material#STONE},
 * {@link Material#ZOMBIE_HEAD}, {@link Material#DIAMOND_SWORD}, …) over {@link Material} enum
 * constants that may be missing at runtime. {@link #of(Material, Material)} is fragile because the
 * preferred constant is already linked at class load.
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
    };

    /**
     * Substitutes only for materials that are genuinely absent on some eras in the 1.17+ range
     * (added later). Not for renames (use alias groups) and not for always-present items (totem,
     * crystal, anchor, netherite).
     */
    private static final Map<String, String> FALLBACKS;
    static {
        Map<String, String> fallbacks = new java.util.HashMap<>();
        fallbacks.put("MACE", "DIAMOND_SWORD");
        fallbacks.put("WIND_CHARGE", "SNOWBALL");
        fallbacks.put("RECOVERY_COMPASS", "COMPASS");
        fallbacks.put("PIGLIN_HEAD", "ZOMBIE_HEAD");
        fallbacks.put("RESIN_BRICK", "BRICK");
        fallbacks.put("BARRIER", "REDSTONE_BLOCK");
        fallbacks.put("TOTEM_OF_UNDYING", "GOLDEN_APPLE");
        fallbacks.put("BLACK_STAINED_GLASS_PANE", "STAINED_GLASS_PANE");
        FALLBACKS = java.util.Collections.unmodifiableMap(fallbacks);
    }

    private static final String TRIM_TEMPLATE_FALLBACK = "GUNPOWDER";
    private static final String TRIM_TEMPLATE_SUFFIX = "_ARMOR_TRIM_SMITHING_TEMPLATE";

    private MaterialCatalog() {}

    /** Whether the named material resolves on this server (after aliases). */
    public static boolean available(String name) {
        return matchPreferred(name) != null;
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

    public static ItemStack stack(String name, Material fallback, int count) {
        return new ItemStack(optional(name, fallback), count);
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
