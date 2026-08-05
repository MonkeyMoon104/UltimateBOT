package com.monkey.ultimatebot.common.model;

import java.util.EnumSet;
import java.util.Set;

final class BuiltInCombatModeCatalog {
    private BuiltInCombatModeCatalog() {}

    static String displayName(String value) {
        return switch (value) {
            case "sword" -> "Sword PvP";
            case "uhc" -> "UHC PvP";
            case "cart" -> "Cart PvP";
            case "crystal" -> "Crystal PvP";
            case "mace" -> "Mace PvP";
            case "water" -> "Water PvP";
            case "axe-shield" -> "Axe & Shield";
            case "netherite-pot" -> "Netherite Pot";
            case "smp" -> "SMP PvP";
            case "trident" -> "Trident PvP";
            default -> throw new IllegalArgumentException("Unknown built-in combat mode: " + value);
        };
    }

    static Set<CombatCapability> capabilities(String value) {
        EnumSet<CombatCapability> capabilities =
                switch (value) {
                    case "sword" ->
                        EnumSet.of(CombatCapability.MELEE, CombatCapability.HEALING, CombatCapability.MOB_TARGETS);
                    case "uhc" ->
                        EnumSet.of(
                                CombatCapability.MELEE,
                                CombatCapability.PROJECTILE,
                                CombatCapability.HEALING,
                                CombatCapability.BUILDING,
                                CombatCapability.MOB_TARGETS);
                    case "cart" ->
                        EnumSet.of(CombatCapability.MELEE, CombatCapability.EXPLOSIVES, CombatCapability.BUILDING);
                    case "crystal" ->
                        EnumSet.of(
                                CombatCapability.MELEE,
                                CombatCapability.EXPLOSIVES,
                                CombatCapability.BUILDING,
                                CombatCapability.HEALING);
                    case "mace" ->
                        EnumSet.of(CombatCapability.MELEE, CombatCapability.BUILDING, CombatCapability.MOB_TARGETS);
                    case "water", "trident" ->
                        EnumSet.of(
                                CombatCapability.MELEE,
                                CombatCapability.PROJECTILE,
                                CombatCapability.WATER,
                                CombatCapability.MOB_TARGETS);
                    case "axe-shield" ->
                        EnumSet.of(CombatCapability.MELEE, CombatCapability.SHIELD, CombatCapability.MOB_TARGETS);
                    case "netherite-pot" ->
                        EnumSet.of(
                                CombatCapability.MELEE,
                                CombatCapability.HEALING,
                                CombatCapability.POTIONS,
                                CombatCapability.MOB_TARGETS);
                    case "smp" ->
                        EnumSet.of(
                                CombatCapability.MELEE,
                                CombatCapability.PROJECTILE,
                                CombatCapability.HEALING,
                                CombatCapability.SHIELD,
                                CombatCapability.MOB_TARGETS);
                    default -> throw new IllegalArgumentException("Unknown built-in combat mode: " + value);
                };
        return Set.copyOf(capabilities);
    }
}
