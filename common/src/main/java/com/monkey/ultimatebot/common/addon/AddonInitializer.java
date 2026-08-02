package com.monkey.ultimatebot.common.addon;

/** Builds the runtime service supplied by an addon factory. */
@FunctionalInterface
public interface AddonInitializer<F, A extends AutoCloseable> {
    A initialize(F factory) throws Exception;
}
