package com.monkey.ultimatebot.access.entity;

import java.lang.reflect.Method;
import java.util.Objects;
import org.bukkit.entity.Entity;
import org.jspecify.annotations.Nullable;

public final class EntityInvulnerableAccess {

    private static final @Nullable Method SET_INVULNERABLE = resolve("setInvulnerable", boolean.class);
    private static final @Nullable Method IS_INVULNERABLE = resolve("isInvulnerable");

    private EntityInvulnerableAccess() {}

    public static void setInvulnerable(Entity entity, boolean invulnerable) {
        Objects.requireNonNull(entity, "entity");
        if (SET_INVULNERABLE == null) {
            return;
        }
        try {
            SET_INVULNERABLE.invoke(entity, invulnerable);
        } catch (ReflectiveOperationException | LinkageError ignored) {

        }
    }

    public static boolean isInvulnerable(Entity entity) {
        Objects.requireNonNull(entity, "entity");
        if (IS_INVULNERABLE == null) {
            return false;
        }
        try {
            Object result = IS_INVULNERABLE.invoke(entity);
            return result instanceof Boolean && ((Boolean) result).booleanValue();
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return false;
        }
    }

    private static @Nullable Method resolve(String name, Class<?>... params) {
        try {
            return Entity.class.getMethod(name, params);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }
}
