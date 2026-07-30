package com.monkey.mcbot.common.guard;

import java.util.UUID;

/** Runtime guard service implemented by the optional platform addon. */
public interface GuardBackend extends AutoCloseable {
    GuardBackend NOOP = new GuardBackend() {};

    default void markBot(Object entity) {}

    default void forgetBot(UUID entityUuid) {}

    @Override
    default void close() {}
}
