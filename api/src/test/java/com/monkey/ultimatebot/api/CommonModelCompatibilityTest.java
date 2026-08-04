package com.monkey.ultimatebot.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.monkey.ultimatebot.api.model.configuration.BotSettings;
import com.monkey.ultimatebot.common.model.BotArmorTier;
import com.monkey.ultimatebot.common.model.BotMode;
import com.monkey.ultimatebot.common.model.BotTargetMode;
import com.monkey.ultimatebot.common.model.DifficultyTier;
import org.junit.jupiter.api.Test;

class CommonModelCompatibilityTest {
    @Test
    void publicModelsUseCanonicalCommonContracts() throws NoSuchMethodException {
        assertEquals(BotMode.EVENT, BotMode.valueOf("EVENT"));
        assertEquals(
                BotArmorTier.class, BotSettings.class.getMethod("armorType").getReturnType());
        assertEquals(
                DifficultyTier.class, BotSettings.class.getMethod("difficulty").getReturnType());
        assertEquals(
                BotTargetMode.class, BotSettings.class.getMethod("targetMode").getReturnType());
    }
}
