package com.monkey.ultimatebot.compat;

/**
 * Maps configured swing waits onto the combat rules of the loaded server.
 *
 * <p>1.9+ has a weapon cooldown; {@code attack-cooldown-ticks} is that wait. 1.8 has no cooldown —
 * the same yaml values would make the bot hit like a 1.9 timed crit instead of click-spam / W-tap
 * PvP. Convert them to click delay (ticks between swings) so 1.8 stays fast.
 */
public final class CombatCadenceAccess {

    private CombatCadenceAccess() {}

    public static boolean hasWeaponCooldown() {
        return MinecraftVersionAccess.isAtLeast(1, 9);
    }

    /**
     * Ticks to wait after a sword swing. {@code configuredCooldownTicks} is the yaml/GUI 1.9 wait.
     */
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
