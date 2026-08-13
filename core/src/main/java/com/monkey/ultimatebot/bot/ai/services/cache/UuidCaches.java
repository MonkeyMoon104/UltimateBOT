package com.monkey.ultimatebot.bot.ai.services.cache;

import com.monkey.ultimatebot.compat.JavaRuntimeAccess;
import com.monkey.ultimatebot.config.RuntimeSettings;
import java.lang.reflect.Method;

/**
 * Picks the shaded Caffeine line at runtime: 2.x on Java 8–10, 3.x on Java 11+.
 *
 * <p>Backends are loaded via {@link Class#forName(String)} so UltimateBot verification never pulls
 * Caffeine classes before the matching relocated implementation is chosen.
 */
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
