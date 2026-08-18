package com.monkey.ultimatebot.common.model;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    ARMOR_TRIM;

    private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)(?:\\.(\\d+))?");

    public static Set<PlatformCapability> none() {
        return Collections.unmodifiableSet(EnumSet.noneOf(PlatformCapability.class));
    }

    public static Set<PlatformCapability> through1_8() {
        return Collections.unmodifiableSet(EnumSet.of(TNT_MINECART));
    }

    public static Set<PlatformCapability> through1_9() {
        return Collections.unmodifiableSet(
                EnumSet.of(OFFHAND, SHIELD, COMBAT_COOLDOWN, SWEEP_ATTACK, END_CRYSTAL, TNT_MINECART));
    }

    public static Set<PlatformCapability> through1_11() {
        EnumSet<PlatformCapability> set = EnumSet.copyOf(through1_9());
        set.add(TOTEM);
        return Collections.unmodifiableSet(set);
    }

    public static Set<PlatformCapability> through1_13() {
        EnumSet<PlatformCapability> set = EnumSet.copyOf(through1_11());
        set.add(TRIDENT);
        return Collections.unmodifiableSet(set);
    }

    public static Set<PlatformCapability> through1_16() {
        EnumSet<PlatformCapability> set = EnumSet.copyOf(through1_13());
        set.add(NETHERITE);
        set.add(RESPAWN_ANCHOR);
        return Collections.unmodifiableSet(set);
    }

    public static Set<PlatformCapability> through1_19() {
        return through1_16();
    }

    public static Set<PlatformCapability> through1_20() {
        EnumSet<PlatformCapability> set = EnumSet.copyOf(through1_19());
        set.add(ARMOR_TRIM);
        return Collections.unmodifiableSet(set);
    }

    public static Set<PlatformCapability> allSupported() {
        return Collections.unmodifiableSet(EnumSet.allOf(PlatformCapability.class));
    }

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
        return allSupported();
    }

    public static Set<PlatformCapability> without(PlatformCapability first, PlatformCapability... rest) {
        EnumSet<PlatformCapability> set = EnumSet.allOf(PlatformCapability.class);
        set.remove(first);
        for (PlatformCapability capability : rest) {
            set.remove(capability);
        }
        return Collections.unmodifiableSet(set);
    }
}
