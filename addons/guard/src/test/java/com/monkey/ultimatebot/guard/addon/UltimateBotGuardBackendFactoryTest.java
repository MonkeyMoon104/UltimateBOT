package com.monkey.ultimatebot.guard.addon;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import com.monkey.ultimatebot.common.guard.GuardBackendContext;
import org.junit.jupiter.api.Test;

class UltimateBotGuardBackendFactoryTest {
    @Test
    void rejectsNonBukkitPluginHandles() {
        GuardBackendContext context = new GuardBackendContext(new Object(), ignored -> false, ignored -> {});

        assertThatIllegalArgumentException()
                .isThrownBy(() -> new UltimateBotGuardBackendFactory().create(context))
                .withMessage("Guard addon requires a Bukkit Plugin handle");
    }
}
