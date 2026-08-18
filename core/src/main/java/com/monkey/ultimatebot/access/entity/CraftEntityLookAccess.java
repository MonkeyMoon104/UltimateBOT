package com.monkey.ultimatebot.access.entity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;
import org.bukkit.entity.Player;

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

            }
        }
    }
}
