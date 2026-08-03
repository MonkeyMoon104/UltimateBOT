package com.monkey.ultimatebot.combat.mode;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class ModeCombatSignalsTest {
    @Test
    void shieldImpactIsConsumedOnlyByItsAttacker() {
        ModeCombatSignals signals = new ModeCombatSignals();
        UUID attacker = UUID.randomUUID();

        signals.recordShieldImpact(attacker);

        assertThat(signals.consumeShieldImpact(UUID.randomUUID())).isFalse();
        assertThat(signals.consumeShieldImpact(attacker)).isTrue();
        assertThat(signals.consumeShieldImpact(attacker)).isFalse();
    }

    @Test
    void clearReleasesPendingAttacker() {
        ModeCombatSignals signals = new ModeCombatSignals();
        UUID attacker = UUID.randomUUID();

        signals.recordShieldImpact(attacker);
        signals.clear();

        assertThat(signals.consumeShieldImpact(attacker)).isFalse();
    }
}
