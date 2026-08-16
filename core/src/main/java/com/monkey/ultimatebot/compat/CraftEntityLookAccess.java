package com.monkey.ultimatebot.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;
import org.bukkit.entity.Player;

/**
 * Sets NMS entity yaw/pitch/head without teleporting. Used on Paper &lt;1.17 where {@code
 * CraftPlayer#setRotation} throws and a same-position teleport would cancel velocity (jerky follow).
 *
 * <p>Obfuscated body/head field names collide across revisions (e.g. {@code aC} is body yaw float
 * on 1.14+ but an {@code int} hurt counter on 1.13). Only write float fields; skip type mismatches.
 */
public final class CraftEntityLookAccess {

    private static final String[] HEAD_FIELDS = {"yHeadRot", "aS", "aA", "aK", "aR"};
    private static final String[] BODY_FIELDS = {"yBodyRot", "aC", "aQ", "aI", "aY"};

    private CraftEntityLookAccess() {}

    public static void setYawPitch(Player player, float yaw, float pitch) {
        Objects.requireNonNull(player, "player");
        try {
            Method getHandle = player.getClass().getMethod("getHandle");
            Object handle = getHandle.invoke(player);
            if (handle == null) {
                return;
            }
            setFloatAlongHierarchy(handle, "yaw", yaw);
            setFloatAlongHierarchy(handle, "pitch", pitch);
            if (!invokeSetHeadRotation(handle, yaw)) {
                setFirstPresentFloat(handle, HEAD_FIELDS, yaw);
            }
            setFirstPresentFloat(handle, BODY_FIELDS, yaw);
        } catch (ReflectiveOperationException | IllegalArgumentException ignored) {
            // Leave look unchanged rather than teleporting (preserves motion).
        }
    }

    private static boolean invokeSetHeadRotation(Object handle, float yaw) {
        try {
            Method method = handle.getClass().getMethod("setHeadRotation", float.class);
            method.invoke(handle, Float.valueOf(yaw));
            return true;
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    private static void setFloatAlongHierarchy(Object handle, String name, float value)
            throws ReflectiveOperationException {
        Class<?> type = handle.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField(name);
                if (field.getType() != float.class && field.getType() != Float.class) {
                    throw new NoSuchFieldException(name + " is not float");
                }
                field.setAccessible(true);
                field.setFloat(handle, value);
                return;
            } catch (NoSuchFieldException ignored) {
                type = type.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    private static void setFirstPresentFloat(Object handle, String[] names, float value) {
        for (String name : names) {
            try {
                setFloatAlongHierarchy(handle, name, value);
                return;
            } catch (ReflectiveOperationException | IllegalArgumentException ignored) {
                // try next
            }
        }
    }
}
