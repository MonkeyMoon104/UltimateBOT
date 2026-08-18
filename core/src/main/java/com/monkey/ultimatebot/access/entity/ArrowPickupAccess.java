package com.monkey.ultimatebot.access.entity;

import java.lang.reflect.Method;
import java.util.Objects;
import org.bukkit.entity.Projectile;
import org.jspecify.annotations.Nullable;

public final class ArrowPickupAccess {

    private static final @Nullable Method SET_PICKUP;
    private static final @Nullable Object DISALLOWED;

    static {
        Resolved resolved = resolve();
        SET_PICKUP = resolved.setter;
        DISALLOWED = resolved.disallowed;
    }

    private ArrowPickupAccess() {}

    public static void disallowPickup(Projectile projectile) {
        Objects.requireNonNull(projectile, "projectile");
        if (SET_PICKUP == null || DISALLOWED == null) {
            return;
        }
        try {
            SET_PICKUP.invoke(projectile, DISALLOWED);
        } catch (ReflectiveOperationException ignored) {

        }
    }

    private static Resolved resolve() {
        Resolved modern = resolveOn("org.bukkit.entity.AbstractArrow");
        if (modern.setter != null) {
            return modern;
        }
        return resolveOn("org.bukkit.entity.Arrow");
    }

    private static Resolved resolveOn(String ownerName) {
        try {
            Class<?> owner = Class.forName(ownerName);
            Class<?> status = Class.forName(ownerName + "$PickupStatus");
            Object disallowed = status.getField("DISALLOWED").get(null);
            Method setter = owner.getMethod("setPickupStatus", status);
            return new Resolved(setter, disallowed);
        } catch (ReflectiveOperationException | ClassCastException ignored) {
            return new Resolved(null, null);
        }
    }

    private static final class Resolved {
        final @Nullable Method setter;
        final @Nullable Object disallowed;

        Resolved(@Nullable Method setter, @Nullable Object disallowed) {
            this.setter = setter;
            this.disallowed = disallowed;
        }
    }
}
