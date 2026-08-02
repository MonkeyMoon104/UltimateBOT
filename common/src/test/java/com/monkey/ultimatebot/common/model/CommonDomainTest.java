package com.monkey.ultimatebot.common.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class CommonDomainTest {
    @Test
    void targetModesExposeCapabilitiesAndCycleDeterministically() {
        assertThat(BotTargetMode.PLAYERS.allowsPlayers()).isTrue();
        assertThat(BotTargetMode.PLAYERS.allowsMobs()).isFalse();
        assertThat(BotTargetMode.PLAYERS.next()).isEqualTo(BotTargetMode.MOBS);
        assertThat(BotTargetMode.MOBS.next()).isEqualTo(BotTargetMode.PLAYERS_AND_MOBS);
        assertThat(BotTargetMode.PLAYERS_AND_MOBS.next()).isEqualTo(BotTargetMode.PLAYERS);
    }

    @Test
    void combatModesExposeTenTypedStrategies() {
        assertThat(CombatMode.values()).hasSize(10);
        assertThat(CombatMode.CRYSTAL.supports(CombatCapability.EXPLOSIVES)).isTrue();
        assertThat(CombatMode.WATER.supports(CombatCapability.WATER)).isTrue();
        assertThat(CombatMode.AXE_SHIELD.supports(CombatCapability.SHIELD)).isTrue();
    }

    @Test
    void combatTuningRejectsUnsafeValues() {
        assertThatThrownBy(() -> CombatTuning.builder().attackRange(8.0D).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("attackRange");
    }
}
