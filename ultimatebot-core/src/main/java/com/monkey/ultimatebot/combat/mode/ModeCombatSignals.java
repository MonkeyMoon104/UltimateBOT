package com.monkey.ultimatebot.combat.mode;

import java.util.Objects;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

final class ModeCombatSignals {
    private @Nullable UUID shieldImpactAttacker;

    void recordShieldImpact(UUID attackerUUID) {
        shieldImpactAttacker = Objects.requireNonNull(attackerUUID, "attackerUUID");
    }

    boolean consumeShieldImpact(UUID attackerUUID) {
        Objects.requireNonNull(attackerUUID, "attackerUUID");
        if (!attackerUUID.equals(shieldImpactAttacker)) {
            return false;
        }
        shieldImpactAttacker = null;
        return true;
    }

    void clear() {
        shieldImpactAttacker = null;
    }
}
