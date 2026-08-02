package com.monkey.ultimatebot.common.guard;

/** Creates the platform guard backend. */
@FunctionalInterface
public interface GuardBackendFactory {
    GuardBackend create(GuardBackendContext context);
}
