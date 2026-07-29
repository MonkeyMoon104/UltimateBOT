package com.monkey.mcbot.bot.ai.controllers.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BotExplosionContextTest {

    @Test
    void suppressesOnlyProtectedBotExplosions() {
        assertFalse(BotExplosionContext.isBlockDamageSuppressed());

        BotExplosionContext.execute(false, () -> {
            assertTrue(BotExplosionContext.isBlockDamageSuppressed());
            return null;
        });

        assertFalse(BotExplosionContext.isBlockDamageSuppressed());
        BotExplosionContext.execute(true, () -> {
            assertFalse(BotExplosionContext.isBlockDamageSuppressed());
            return null;
        });
    }

    @Test
    void alwaysClearsThreadStateAfterFailure() {
        assertThrows(IllegalStateException.class, () ->
                BotExplosionContext.execute(false, () -> {
                    throw new IllegalStateException("boom");
                }));
        assertFalse(BotExplosionContext.isBlockDamageSuppressed());
    }
}
