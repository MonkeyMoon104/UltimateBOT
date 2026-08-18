package com.monkey.ultimatebot.common.guard;

@FunctionalInterface
public interface GuardBackendFactory {
    GuardBackend create(GuardBackendContext context);
}
