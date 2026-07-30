package com.monkey.mcbot.guard.addon;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import com.monkey.mcbot.common.guard.GuardBackendContext;
import org.junit.jupiter.api.Test;

class MinecraftBotGuardBackendFactoryTest {
    @Test
    void rejectsNonBukkitPluginHandles() {
        GuardBackendContext context = new GuardBackendContext(new Object(), ignored -> false, ignored -> {});

        assertThatIllegalArgumentException()
                .isThrownBy(() -> new MinecraftBotGuardBackendFactory().create(context))
                .withMessage("Guard addon requires a Bukkit Plugin handle");
    }
}
