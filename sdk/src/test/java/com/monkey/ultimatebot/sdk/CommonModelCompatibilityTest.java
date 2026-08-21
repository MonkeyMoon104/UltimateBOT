package com.monkey.ultimatebot.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.monkey.ultimatebot.common.model.bot.BotArmorTier;
import com.monkey.ultimatebot.common.model.bot.BotTargetMode;
import com.monkey.ultimatebot.common.model.combat.DifficultyTier;
import com.monkey.ultimatebot.sdk.model.request.BotSpawnRequest;
import org.junit.jupiter.api.Test;

class CommonModelCompatibilityTest {
    @Test
    void sdkUsesCanonicalCommonTypesDirectly() throws NoSuchMethodException {
        assertEquals(
                BotArmorTier.class, BotSpawnRequest.class.getMethod("armor").getReturnType());
        assertEquals(
                BotTargetMode.class,
                BotSpawnRequest.class.getMethod("targetMode").getReturnType());
        assertEquals(
                DifficultyTier.class,
                BotSpawnRequest.class.getMethod("difficulty").getReturnType());
    }
}
