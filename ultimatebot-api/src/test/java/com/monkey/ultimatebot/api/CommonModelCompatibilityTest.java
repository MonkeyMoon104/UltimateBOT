package com.monkey.ultimatebot.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.monkey.ultimatebot.api.model.BotMode;
import com.monkey.ultimatebot.api.model.BotTargetMode;
import org.junit.jupiter.api.Test;

class CommonModelCompatibilityTest {
    @Test
    void publicModelsRoundTripThroughCommonContracts() {
        assertEquals(BotMode.EVENT, BotMode.fromCommon(BotMode.EVENT.toCommon()));
        assertEquals(BotTargetMode.MOBS, BotTargetMode.fromCommon(BotTargetMode.MOBS.toCommon()));
        assertEquals(BotTargetMode.PLAYERS_AND_MOBS, BotTargetMode.MOBS.next());
    }
}
