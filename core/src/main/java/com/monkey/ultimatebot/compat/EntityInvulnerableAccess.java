package com.monkey.ultimatebot.compat;

import java.lang.reflect.Method;
import java.util.Objects;
import org.bukkit.entity.Entity;
import org.jspecify.annotations.Nullable;

/**
 * {@link Entity#setInvulnerable(boolean)} / {@link Entity#isInvulnerable()} across Bukkit revisions.
 *
 * <p>Both methods exist from 1.9. Spigot 1.8's {@code Entity} has neither — a hard-linked call
 * throws {@link NoSuchMethodError}. Probe and invoke reflectively so 1.8 never links the method.
 * When absent, entities are treated as vulnerable (vanilla 1.8 default).
 */
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
            // 1.8 / mismatched Craft: leave vanilla vulnerability.
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
