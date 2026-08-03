package com.monkey.ultimatebot.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.monkey.ultimatebot.common.model.BotArmorTier;
import com.monkey.ultimatebot.common.model.BotTargetMode;
import com.monkey.ultimatebot.sdk.model.type.SdkBotArmor;
import com.monkey.ultimatebot.sdk.model.type.SdkBotTargetMode;
import org.junit.jupiter.api.Test;

class CommonModelCompatibilityTest {
    @Test
    void sdkModelsExposeCanonicalCommonValues() {
        assertEquals(BotArmorTier.GOLDEN, SdkBotArmor.GOLD.toCommon());
        assertEquals(BotTargetMode.MOBS, SdkBotTargetMode.MOBS.toCommon());
        assertEquals(SdkBotTargetMode.PLAYERS_AND_MOBS, SdkBotTargetMode.fromCommon(BotTargetMode.PLAYERS_AND_MOBS));
    }
}
