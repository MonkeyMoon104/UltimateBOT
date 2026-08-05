package com.monkey.ultimatebot.api.extension;

/** Idempotent registration handle returned for every installed extension. */
@FunctionalInterface
public interface ExtensionRegistration extends AutoCloseable {
    @Override
    void close();
}
