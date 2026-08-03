package com.monkey.ultimatebot.bot;

import static org.assertj.core.api.Assertions.assertThat;

import com.monkey.ultimatebot.api.model.BotTargetMode;
import org.junit.jupiter.api.Test;

class BotTargetModeTest {

    @Test
    void cyclesThroughEveryModeAndReturnsToPlayers() {
        assertThat(BotTargetMode.PLAYERS.next()).isSameAs(BotTargetMode.MOBS);
        assertThat(BotTargetMode.MOBS.next()).isSameAs(BotTargetMode.PLAYERS_AND_MOBS);
        assertThat(BotTargetMode.PLAYERS_AND_MOBS.next()).isSameAs(BotTargetMode.PLAYERS);
    }

    @Test
    void exposesAllowedTargetCategories() {
        assertThat(BotTargetMode.PLAYERS.allowsPlayers()).isTrue();
        assertThat(BotTargetMode.PLAYERS.allowsMobs()).isFalse();
        assertThat(BotTargetMode.MOBS.allowsPlayers()).isFalse();
        assertThat(BotTargetMode.MOBS.allowsMobs()).isTrue();
        assertThat(BotTargetMode.PLAYERS_AND_MOBS.allowsPlayers()).isTrue();
        assertThat(BotTargetMode.PLAYERS_AND_MOBS.allowsMobs()).isTrue();
    }
}
