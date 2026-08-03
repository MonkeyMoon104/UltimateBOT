package com.monkey.ultimatebot.combat.mode.runtime;

import java.util.Objects;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public final class ModeCombatSignals {
    private @Nullable UUID shieldImpactAttacker;

    public void recordShieldImpact(UUID attackerUUID) {
        shieldImpactAttacker = Objects.requireNonNull(attackerUUID, "attackerUUID");
    }

    public boolean consumeShieldImpact(UUID attackerUUID) {
        Objects.requireNonNull(attackerUUID, "attackerUUID");
        if (!attackerUUID.equals(shieldImpactAttacker)) {
            return false;
        }
        shieldImpactAttacker = null;
        return true;
    }

    public void clear() {
        shieldImpactAttacker = null;
    }
}
