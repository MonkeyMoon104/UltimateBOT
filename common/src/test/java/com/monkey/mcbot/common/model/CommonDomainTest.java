package com.monkey.mcbot.common.model;

import static org.assertj.core.api.Assertions.assertThat;

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
}
