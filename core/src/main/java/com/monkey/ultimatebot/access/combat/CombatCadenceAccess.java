package com.monkey.ultimatebot.access.combat;

import com.monkey.ultimatebot.access.runtime.MinecraftVersionAccess;

public final class CombatCadenceAccess {

    private CombatCadenceAccess() {}

    public static boolean hasWeaponCooldown() {
        return MinecraftVersionAccess.isAtLeast(1, 9);
    }

    public static int swingDelayTicks(int configuredCooldownTicks) {
        int configured = Math.max(0, configuredCooldownTicks);
        if (hasWeaponCooldown()) {
            return configured;
        }
        if (configured >= 16) {
            return 3;
        }
        if (configured >= 11) {
            return 2;
        }
        if (configured >= 6) {
            return 1;
        }
        return 0;
    }
}
