package com.monkey.ultimatebot.common.model;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Server-era gameplay features that may be absent on older Minecraft versions. */
public enum PlatformCapability {
    COMBAT_COOLDOWN,
    SHIELD,
    END_CRYSTAL,
    TOTEM,
    RESPAWN_ANCHOR,
    TRIDENT,
    MACE,
    WIND_CHARGE,
    TNT_MINECART,
    NETHERITE,
    OFFHAND,
    SWEEP_ATTACK,
    /** Armor trim smithing templates / trim UI (Minecraft 1.20+). */
    ARMOR_TRIM;

    private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)(?:\\.(\\d+))?");

    /** No platform combat features — empty set (e.g. unknown version fallback). */
    public static Set<PlatformCapability> none() {
        return Collections.unmodifiableSet(EnumSet.noneOf(PlatformCapability.class));
    }

    /**
     * Features available on Minecraft 1.8.x for UltimateBot's catalog.
     *
     * <p>No offhand, shield, totem, netherite, combat cooldown, trident, end crystal item, or mace.
     * {@link #TNT_MINECART} is present so cart kits can fall back to diamond.
     */
    public static Set<PlatformCapability> through1_8() {
        return Collections.unmodifiableSet(EnumSet.of(TNT_MINECART));
    }

    /**
     * Features from Minecraft 1.9 through 1.10.x: offhand, shield, combat cooldown, sweep, plus
     * legacy TNT minecart.
     */
    public static Set<PlatformCapability> through1_9() {
        return Collections.unmodifiableSet(
                EnumSet.of(OFFHAND, SHIELD, COMBAT_COOLDOWN, SWEEP_ATTACK, TNT_MINECART));
    }

    /** Features from Minecraft 1.11 through 1.12.x: {@link #through1_9()} plus {@link #TOTEM}. */
    public static Set<PlatformCapability> through1_11() {
        EnumSet<PlatformCapability> set = EnumSet.copyOf(through1_9());
        set.add(TOTEM);
        return Collections.unmodifiableSet(set);
    }

    /**
     * Features from Minecraft 1.13 through 1.15.x: {@link #through1_11()} plus {@link #TRIDENT} and
     * {@link #END_CRYSTAL} (crystal item + obsidian; respawn anchors stay 1.16+).
     */
    public static Set<PlatformCapability> through1_13() {
        EnumSet<PlatformCapability> set = EnumSet.copyOf(through1_11());
        set.add(TRIDENT);
        set.add(END_CRYSTAL);
        return Collections.unmodifiableSet(set);
    }

    /**
     * Features from Minecraft 1.16 through 1.16.x: {@link #through1_13()} plus {@link #NETHERITE}
     * and {@link #RESPAWN_ANCHOR}.
     *
     * <p>Same membership as {@link #through1_19()} — no new enum members appear until 1.20.
     */
    public static Set<PlatformCapability> through1_16() {
        EnumSet<PlatformCapability> set = EnumSet.copyOf(through1_13());
        set.add(NETHERITE);
        set.add(RESPAWN_ANCHOR);
        return Collections.unmodifiableSet(set);
    }

    /**
     * Capabilities present from 1.17 through 1.19.x — no {@link #MACE}, {@link #WIND_CHARGE}, or
     * {@link #ARMOR_TRIM}.
     */
    public static Set<PlatformCapability> through1_19() {
        return through1_16();
    }

    /**
     * Capabilities present from 1.20 through 1.20.x — everything except {@link #MACE} and {@link
     * #WIND_CHARGE} (includes {@link #ARMOR_TRIM}).
     */
    public static Set<PlatformCapability> through1_20() {
        EnumSet<PlatformCapability> set = EnumSet.copyOf(through1_19());
        set.add(ARMOR_TRIM);
        return Collections.unmodifiableSet(set);
    }

    /** Full capability set for 1.21+ (includes mace, wind charge, and armor trim). */
    public static Set<PlatformCapability> allSupported() {
        return Collections.unmodifiableSet(EnumSet.allOf(PlatformCapability.class));
    }

    /**
     * Picks the era capability set for a Minecraft version string (e.g. {@code 1.8.8}, {@code
     * 1.16.5}, {@code 26.1}). Unknown / unparseable versions yield {@link #none()}.
     */
    public static Set<PlatformCapability> forMinecraftVersion(String version) {
        if (version == null || version.isEmpty() || "unknown".equalsIgnoreCase(version)) {
            return none();
        }
        String normalized = version.toLowerCase(Locale.ROOT).trim();
        if (normalized.startsWith("26.")) {
            return allSupported();
        }
        Matcher matcher = VERSION_PATTERN.matcher(normalized);
        if (!matcher.find()) {
            return none();
        }
        int major = Integer.parseInt(matcher.group(1));
        int minor = Integer.parseInt(matcher.group(2));
        if (major > 1) {
            return allSupported();
        }
        if (major < 1) {
            return none();
        }
        if (minor <= 8) {
            return through1_8();
        }
        if (minor <= 10) {
            return through1_9();
        }
        if (minor <= 12) {
            return through1_11();
        }
        if (minor <= 15) {
            return through1_13();
        }
        if (minor <= 19) {
            return through1_16();
        }
        if (minor == 20) {
            return through1_20();
        }
        // 1.21+
        return allSupported();
    }

    /** Full set minus the given capabilities. */
    public static Set<PlatformCapability> without(PlatformCapability first, PlatformCapability... rest) {
        EnumSet<PlatformCapability> set = EnumSet.allOf(PlatformCapability.class);
        set.remove(first);
        for (PlatformCapability capability : rest) {
            set.remove(capability);
        }
        return Collections.unmodifiableSet(set);
    }
}
