package com.monkey.ultimatebot.common.model;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import com.monkey.ultimatebot.common.util.ImmutableCollections;

final class BuiltInCombatModeCatalog {
    private BuiltInCombatModeCatalog() {}

    static String displayName(String value) {
        switch (value) {
            case "sword":
                return "Sword PvP";
            case "uhc":
                return "UHC PvP";
            case "cart":
                return "Cart PvP";
            case "crystal":
                return "Crystal PvP";
            case "mace":
                return "Mace PvP";
            case "water":
                return "Water PvP";
            case "axe-shield":
                return "Axe & Shield";
            case "netherite-pot":
                return "Netherite Pot";
            case "smp":
                return "SMP PvP";
            case "trident":
                return "Trident PvP";
            default:
                throw new IllegalArgumentException("Unknown built-in combat mode: " + value);
        }
    }

    /** Platform features required for a built-in mode to be playable on the loaded server. */
    static Set<PlatformCapability> requiredPlatformCapabilities(String value) {
        EnumSet<PlatformCapability> required;
        switch (value) {
            case "sword":
            case "water":
                required = EnumSet.noneOf(PlatformCapability.class);
                break;
            case "uhc":
                // Classic UHC is 1.8 (sword, gapple, cobweb). Shield/offhand are 1.9 kit extras, not
                // a mode requirement — ModeKit.offHand already no-ops when the slot is missing.
                required = EnumSet.noneOf(PlatformCapability.class);
                break;
            case "axe-shield":
                required = EnumSet.of(
                        PlatformCapability.SHIELD,
                        PlatformCapability.OFFHAND,
                        PlatformCapability.COMBAT_COOLDOWN);
                break;
            case "smp":
                required = EnumSet.of(
                        PlatformCapability.SHIELD, PlatformCapability.TOTEM, PlatformCapability.OFFHAND);
                break;
            case "netherite-pot":
                required = EnumSet.of(
                        PlatformCapability.NETHERITE, PlatformCapability.TOTEM, PlatformCapability.OFFHAND);
                break;
            case "cart":
                required = EnumSet.of(PlatformCapability.TNT_MINECART);
                break;
            case "crystal":
                required = EnumSet.of(PlatformCapability.END_CRYSTAL);
                break;
            case "trident":
                required = EnumSet.of(PlatformCapability.TRIDENT);
                break;
            case "mace":
                required = EnumSet.of(PlatformCapability.MACE, PlatformCapability.WIND_CHARGE);
                break;
            default:
                throw new IllegalArgumentException("Unknown built-in combat mode: " + value);
        }
        return required.isEmpty()
                ? ImmutableCollections.emptySet()
                : Collections.unmodifiableSet(EnumSet.copyOf(required));
    }

    static Set<CombatCapability> capabilities(String value) {
        EnumSet<CombatCapability> capabilities;
        switch (value) {
            case "sword":
                capabilities =
                        EnumSet.of(CombatCapability.MELEE, CombatCapability.HEALING, CombatCapability.MOB_TARGETS);
                break;
            case "uhc":
                capabilities = EnumSet.of(
                        CombatCapability.MELEE,
                        CombatCapability.PROJECTILE,
                        CombatCapability.HEALING,
                        CombatCapability.BUILDING,
                        CombatCapability.MOB_TARGETS);
                break;
            case "cart":
                capabilities =
                        EnumSet.of(CombatCapability.MELEE, CombatCapability.EXPLOSIVES, CombatCapability.BUILDING);
                break;
            case "crystal":
                capabilities = EnumSet.of(
                        CombatCapability.MELEE,
                        CombatCapability.EXPLOSIVES,
                        CombatCapability.BUILDING,
                        CombatCapability.HEALING);
                break;
            case "mace":
                capabilities =
                        EnumSet.of(CombatCapability.MELEE, CombatCapability.BUILDING, CombatCapability.MOB_TARGETS);
                break;
            case "water":
            case "trident":
                capabilities = EnumSet.of(
                        CombatCapability.MELEE,
                        CombatCapability.PROJECTILE,
                        CombatCapability.WATER,
                        CombatCapability.MOB_TARGETS);
                break;
            case "axe-shield":
                capabilities =
                        EnumSet.of(CombatCapability.MELEE, CombatCapability.SHIELD, CombatCapability.MOB_TARGETS);
                break;
            case "netherite-pot":
                capabilities = EnumSet.of(
                        CombatCapability.MELEE,
                        CombatCapability.HEALING,
                        CombatCapability.POTIONS,
                        CombatCapability.MOB_TARGETS);
                break;
            case "smp":
                capabilities = EnumSet.of(
                        CombatCapability.MELEE,
                        CombatCapability.PROJECTILE,
                        CombatCapability.HEALING,
                        CombatCapability.SHIELD,
                        CombatCapability.MOB_TARGETS);
                break;
            default:
                throw new IllegalArgumentException("Unknown built-in combat mode: " + value);
        }
        return ImmutableCollections.copyOf(capabilities);
    }
}
