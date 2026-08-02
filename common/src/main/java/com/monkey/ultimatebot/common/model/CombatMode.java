package com.monkey.ultimatebot.common.model;

import java.util.EnumSet;
import java.util.Set;

/** Canonical combat modes supported by UltimateBot. */
public enum CombatMode {
    SWORD("Sword PvP", CombatCapability.MELEE, CombatCapability.HEALING, CombatCapability.MOB_TARGETS),
    UHC(
            "UHC PvP",
            CombatCapability.MELEE,
            CombatCapability.PROJECTILE,
            CombatCapability.HEALING,
            CombatCapability.BUILDING,
            CombatCapability.MOB_TARGETS),
    CART("Cart PvP", CombatCapability.MELEE, CombatCapability.EXPLOSIVES, CombatCapability.BUILDING),
    CRYSTAL(
            "Crystal PvP",
            CombatCapability.MELEE,
            CombatCapability.EXPLOSIVES,
            CombatCapability.BUILDING,
            CombatCapability.HEALING),
    MACE("Mace PvP", CombatCapability.MELEE, CombatCapability.BUILDING, CombatCapability.MOB_TARGETS),
    WATER(
            "Water PvP",
            CombatCapability.MELEE,
            CombatCapability.PROJECTILE,
            CombatCapability.WATER,
            CombatCapability.MOB_TARGETS),
    AXE_SHIELD("Axe & Shield", CombatCapability.MELEE, CombatCapability.SHIELD, CombatCapability.MOB_TARGETS),
    NETHERITE_POT(
            "Netherite Pot",
            CombatCapability.MELEE,
            CombatCapability.HEALING,
            CombatCapability.POTIONS,
            CombatCapability.MOB_TARGETS),
    SMP(
            "SMP PvP",
            CombatCapability.MELEE,
            CombatCapability.PROJECTILE,
            CombatCapability.HEALING,
            CombatCapability.SHIELD,
            CombatCapability.MOB_TARGETS),
    TRIDENT(
            "Trident PvP",
            CombatCapability.MELEE,
            CombatCapability.PROJECTILE,
            CombatCapability.WATER,
            CombatCapability.MOB_TARGETS);

    private final String displayName;
    private final int capabilityMask;

    CombatMode(String displayName, CombatCapability firstCapability, CombatCapability... remainingCapabilities) {
        this.displayName = displayName;
        EnumSet<CombatCapability> values = EnumSet.of(firstCapability, remainingCapabilities);
        this.capabilityMask = values.stream().mapToInt(CombatCapability::mask).reduce(0, (left, right) -> left | right);
    }

    public String displayName() {
        return displayName;
    }

    public Set<CombatCapability> capabilities() {
        EnumSet<CombatCapability> values = EnumSet.noneOf(CombatCapability.class);
        for (CombatCapability capability : CombatCapability.values()) {
            if (supports(capability)) {
                values.add(capability);
            }
        }
        return Set.copyOf(values);
    }

    public boolean supports(CombatCapability capability) {
        CombatCapability requiredCapability = java.util.Objects.requireNonNull(capability, "capability");
        return (capabilityMask & requiredCapability.mask()) != 0;
    }
}
