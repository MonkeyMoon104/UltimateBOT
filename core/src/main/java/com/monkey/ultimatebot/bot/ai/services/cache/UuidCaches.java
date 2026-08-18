package com.monkey.ultimatebot.bot.ai.services.cache;

import com.monkey.ultimatebot.access.runtime.JavaRuntimeAccess;
import com.monkey.ultimatebot.config.RuntimeSettings;
import java.lang.reflect.Method;

public final class UuidCaches {

    private UuidCaches() {}

    @SuppressWarnings("unchecked")
    public static <V> UuidCache<V> create(RuntimeSettings.CacheSettings settings) {
        String implementation = JavaRuntimeAccess.isAtLeast(11)
                ? "com.monkey.ultimatebot.bot.ai.services.cache.Caffeine3UuidCache"
                : "com.monkey.ultimatebot.bot.ai.services.cache.Caffeine2UuidCache";
        try {
            Class<?> type = Class.forName(implementation);
            Method create = type.getMethod("create", RuntimeSettings.CacheSettings.class);
            return (UuidCache<V>) create.invoke(null, settings);
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException(
                    "Failed to load Caffeine cache backend '"
                            + implementation
                            + "' for Java "
                            + JavaRuntimeAccess.major(),
                    error);
        }
    }
}
