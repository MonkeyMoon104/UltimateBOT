package com.monkey.ultimatebot.access.combat;

import com.monkey.ultimatebot.access.runtime.MinecraftVersionAccess;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

public final class AttackStrengthAccess {

    private static final boolean HAS_BUKKIT_COOLDOWN = hasNoArgFloat(Player.class, "getAttackCooldown");
    private static final Object LOCK = new Object();
    private static volatile boolean resolved;
    private static volatile @Nullable Field tickerField;

    private AttackStrengthAccess() {}

    public static void chargeFull(Player player) {
        Objects.requireNonNull(player, "player");
        if (!MinecraftVersionAccess.isAtLeast(1, 9)) {
            return;
        }
        if (HAS_BUKKIT_COOLDOWN && bukkitCooldown(player) >= 0.99F) {
            return;
        }
        Object handle = nmsHandle(player);
        if (handle == null) {
            return;
        }
        Field ticker = ticker(handle);
        if (ticker == null) {
            return;
        }
        try {
            int current = ticker.getInt(handle);
            if (current < 1000) {
                ticker.setInt(handle, 1000);
            }
        } catch (IllegalAccessException ignored) {

        }
    }

    private static float bukkitCooldown(Player player) {
        try {
            Object value = player.getClass().getMethod("getAttackCooldown").invoke(player);
            if (value instanceof Float) {
                return ((Float) value).floatValue();
            }
        } catch (ReflectiveOperationException ignored) {

        }
        return 0.0F;
    }

    private static @Nullable Object nmsHandle(Player player) {
        try {
            return player.getClass().getMethod("getHandle").invoke(player);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static @Nullable Field ticker(Object handle) {
        Field cached = tickerField;
        if (resolved) {
            return cached;
        }
        synchronized (LOCK) {
            if (resolved) {
                return tickerField;
            }
            tickerField = resolveTicker(handle);
            resolved = true;
            return tickerField;
        }
    }

    private static @Nullable Field resolveTicker(Object handle) {
        Method scale = findScaleMethod(handle.getClass());
        String[] names = {"attackStrengthTicker", "at", "aB", "aD", "aH", "aE", "aF"};
        for (int i = 0; i < names.length; i++) {
            Field field = findIntField(handle.getClass(), names[i]);
            if (field == null) {
                continue;
            }
            if (scale == null) {
                return field;
            }
            try {
                int previous = field.getInt(handle);
                float before = invokeScale(scale, handle);
                field.setInt(handle, 1000);
                float after = invokeScale(scale, handle);
                field.setInt(handle, previous);
                if (after > before && after >= 0.99F) {
                    return field;
                }
            } catch (ReflectiveOperationException ignored) {

            }
        }
        return findIntField(handle.getClass(), "attackStrengthTicker");
    }

    private static @Nullable Method findScaleMethod(Class<?> type) {
        Class<?> cursor = type;
        while (cursor != null && cursor != Object.class) {
            try {
                Method named = cursor.getDeclaredMethod("getAttackCooldown", float.class);
                named.setAccessible(true);
                return named;
            } catch (NoSuchMethodException ignored) {

            }
            try {
                Method named = cursor.getDeclaredMethod("getAttackStrengthScale", float.class);
                named.setAccessible(true);
                return named;
            } catch (NoSuchMethodException ignored) {

            }
            cursor = cursor.getSuperclass();
        }
        return null;
    }

    private static float invokeScale(Method scale, Object handle) throws ReflectiveOperationException {
        Object value = scale.invoke(handle, Float.valueOf(0.5F));
        if (value instanceof Float) {
            return ((Float) value).floatValue();
        }
        return 0.0F;
    }

    private static @Nullable Field findIntField(Class<?> type, String name) {
        Class<?> cursor = type;
        while (cursor != null && cursor != Object.class) {
            try {
                Field field = cursor.getDeclaredField(name);
                if (field.getType() == int.class) {
                    field.setAccessible(true);
                    return field;
                }
            } catch (NoSuchFieldException ignored) {

            }
            cursor = cursor.getSuperclass();
        }
        return null;
    }

    private static boolean hasNoArgFloat(Class<?> type, String name) {
        try {
            return type.getMethod(name).getReturnType() == float.class;
        } catch (NoSuchMethodException ignored) {
            return false;
        }
    }
}
