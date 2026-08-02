package com.monkey.ultimatebot.guard.addon;

import com.monkey.ultimatebot.common.guard.GuardBackend;
import com.monkey.ultimatebot.common.guard.GuardBackendContext;
import com.monkey.ultimatebot.common.guard.GuardBackendFactory;

/** Creates the hardcoded Paper guard implementation. */
public final class UltimateBotGuardBackendFactory implements GuardBackendFactory {
    @Override
    public GuardBackend create(GuardBackendContext context) {
        return new PaperGuardBackend(context);
    }
}
