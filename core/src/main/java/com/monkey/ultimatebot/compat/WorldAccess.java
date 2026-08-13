package com.monkey.ultimatebot.compat;

import java.lang.reflect.Method;
import java.util.Objects;
import org.bukkit.World;
import org.jspecify.annotations.Nullable;

/**
 * Cross-version {@link World} accessors.
 *
 * <p>On modern Paper, {@code World} extends {@code org.bukkit.generator.WorldInfo} and methods such
 * as {@code getName}/{@code getMinHeight}/{@code getMaxHeight} are declared there. Compiling
 * against that API embeds {@code WorldInfo} in the constant pool; loading those call sites on Paper
 * 1.17 (no {@code WorldInfo}) throws {@link NoClassDefFoundError}. Always go through this helper
 * for those members — never hard-link them from shared code.
 */
public final class WorldAccess {

    private static final @Nullable Method GET_NAME = resolve("getName");
    private static final @Nullable Method GET_UID = resolve("getUID");
    private static final @Nullable Method GET_MIN_HEIGHT = resolve("getMinHeight");
    private static final @Nullable Method GET_MAX_HEIGHT = resolve("getMaxHeight");

    private WorldAccess() {}

    public static String name(World world) {
        Objects.requireNonNull(world, "world");
        Object value = invoke(GET_NAME, world);
        if (value instanceof String) {
            String name = (String) value;
            if (!name.isEmpty()) {
                return name;
            }
        }
        Object uid = invoke(GET_UID, world);
        return uid != null ? uid.toString() : "world";
    }

    public static int minHeight(World world) {
        Objects.requireNonNull(world, "world");
        Object value = invoke(GET_MIN_HEIGHT, world);
        if (value instanceof Integer) {
            return (Integer) value;
        }
        return 0;
    }

    public static int maxHeight(World world) {
        Objects.requireNonNull(world, "world");
        Object value = invoke(GET_MAX_HEIGHT, world);
        if (value instanceof Integer) {
            return (Integer) value;
        }
        return 256;
    }

    private static @Nullable Method resolve(String name) {
        try {
            return World.class.getMethod(name);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    private static @Nullable Object invoke(@Nullable Method method, World world) {
        if (method == null) {
            return null;
        }
        try {
            return method.invoke(world);
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return null;
        }
    }
}
