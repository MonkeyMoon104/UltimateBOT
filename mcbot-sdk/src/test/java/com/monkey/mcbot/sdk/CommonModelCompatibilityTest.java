package com.monkey.mcbot.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.monkey.mcbot.common.model.BotArmorTier;
import com.monkey.mcbot.common.model.BotTargetMode;
import com.monkey.mcbot.sdk.model.SdkBotArmor;
import com.monkey.mcbot.sdk.model.SdkBotTargetMode;
import org.junit.jupiter.api.Test;

class CommonModelCompatibilityTest {
    @Test
    void sdkModelsExposeCanonicalCommonValues() {
        assertEquals(BotArmorTier.GOLDEN, SdkBotArmor.GOLD.toCommon());
        assertEquals(BotTargetMode.MOBS, SdkBotTargetMode.MOBS.toCommon());
        assertEquals(SdkBotTargetMode.PLAYERS_AND_MOBS, SdkBotTargetMode.fromCommon(BotTargetMode.PLAYERS_AND_MOBS));
    }
}
