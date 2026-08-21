package com.monkey.ultimatebot.common.model.combat;

public enum CombatCapability {
    MELEE(1),
    PROJECTILE(1 << 1),
    HEALING(1 << 2),
    BUILDING(1 << 3),
    EXPLOSIVES(1 << 4),
    WATER(1 << 5),
    SHIELD(1 << 6),
    POTIONS(1 << 7),
    MOB_TARGETS(1 << 8);

    private final int mask;

    CombatCapability(int mask) {
        this.mask = mask;
    }

    int mask() {
        return mask;
    }
}
