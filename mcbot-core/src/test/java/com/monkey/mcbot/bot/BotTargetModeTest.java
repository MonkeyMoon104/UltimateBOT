package com.monkey.mcbot.bot;

import com.monkey.mcbot.api.model.BotTargetMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BotTargetModeTest {

    @Test
    void cyclesThroughEveryModeAndReturnsToPlayers() {
        assertSame(BotTargetMode.MOBS, BotTargetMode.PLAYERS.next());
        assertSame(BotTargetMode.PLAYERS_AND_MOBS, BotTargetMode.MOBS.next());
        assertSame(BotTargetMode.PLAYERS, BotTargetMode.PLAYERS_AND_MOBS.next());
    }

    @Test
    void exposesAllowedTargetCategories() {
        assertTrue(BotTargetMode.PLAYERS.allowsPlayers());
        assertFalse(BotTargetMode.PLAYERS.allowsMobs());
        assertFalse(BotTargetMode.MOBS.allowsPlayers());
        assertTrue(BotTargetMode.MOBS.allowsMobs());
        assertTrue(BotTargetMode.PLAYERS_AND_MOBS.allowsPlayers());
        assertTrue(BotTargetMode.PLAYERS_AND_MOBS.allowsMobs());
    }
}
