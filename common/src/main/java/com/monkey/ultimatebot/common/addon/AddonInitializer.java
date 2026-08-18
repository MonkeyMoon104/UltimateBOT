package com.monkey.ultimatebot.common.addon;

@FunctionalInterface
public interface AddonInitializer<F, A extends AutoCloseable> {
    A initialize(F factory) throws Exception;
}
