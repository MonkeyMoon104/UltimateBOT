package com.monkey.ultimatebot.common.guard;

import java.util.UUID;

public interface GuardBackend extends AutoCloseable {
    GuardBackend NOOP = new GuardBackend() {};

    default void markBot(Object entity) {}

    default void forgetBot(UUID entityUuid) {}

    @Override
    default void close() {}
}
