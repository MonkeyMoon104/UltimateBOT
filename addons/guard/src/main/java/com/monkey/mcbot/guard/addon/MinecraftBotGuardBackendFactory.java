package com.monkey.mcbot.guard.addon;

import com.monkey.mcbot.common.guard.GuardBackend;
import com.monkey.mcbot.common.guard.GuardBackendContext;
import com.monkey.mcbot.common.guard.GuardBackendFactory;

/** Creates the hardcoded Paper guard implementation. */
public final class MinecraftBotGuardBackendFactory implements GuardBackendFactory {
    @Override
    public GuardBackend create(GuardBackendContext context) {
        return new PaperGuardBackend(context);
    }
}
