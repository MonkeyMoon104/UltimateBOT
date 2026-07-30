package com.monkey.mcbot.common.guard;

/** Creates the platform guard backend. */
@FunctionalInterface
public interface GuardBackendFactory {
    GuardBackend create(GuardBackendContext context);
}
