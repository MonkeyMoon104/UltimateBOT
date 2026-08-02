package com.monkey.ultimatebot.bot.ai.controllers.combat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class BotExplosionContextTest {

    @Test
    void suppressesOnlyProtectedBotExplosions() {
        assertThat(BotExplosionContext.isBlockDamageSuppressed()).isFalse();

        BotExplosionContext.execute(false, () -> {
            assertThat(BotExplosionContext.isBlockDamageSuppressed()).isTrue();
            return null;
        });

        assertThat(BotExplosionContext.isBlockDamageSuppressed()).isFalse();
        BotExplosionContext.execute(true, () -> {
            assertThat(BotExplosionContext.isBlockDamageSuppressed()).isFalse();
            return null;
        });
    }

    @Test
    void alwaysClearsThreadStateAfterFailure() {
        assertThatThrownBy(() -> BotExplosionContext.execute(false, () -> {
                    throw new IllegalStateException("boom");
                }))
                .isInstanceOf(IllegalStateException.class);
        assertThat(BotExplosionContext.isBlockDamageSuppressed()).isFalse();
    }
}
