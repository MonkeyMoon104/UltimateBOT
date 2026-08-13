package com.monkey.ultimatebot.compat;

import java.lang.reflect.Method;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

/**
 * Cross-version block-break progress packets.
 *
 * <p>{@code Player#sendBlockDamage(Location, float, int)} (source entity id) exists on newer Paper;
 * 1.18.x only has the two-arg overload. Dual-path so UHC/web break FX never hard-links the 3-arg
 * method.
 */
public final class BlockDamageAccess {

    private static final @Nullable Method SEND_WITH_SOURCE_ID = resolveWithSourceId();
    private static final @Nullable Method SEND_SIMPLE = resolveSimple();

    private BlockDamageAccess() {}

    public static void send(Player viewer, Location location, float progress, int sourceEntityId) {
        Objects.requireNonNull(viewer, "viewer");
        Objects.requireNonNull(location, "location");
        float clamped = Math.min(1.0F, Math.max(0.0F, progress));
        if (SEND_WITH_SOURCE_ID != null) {
            try {
                SEND_WITH_SOURCE_ID.invoke(viewer, location, clamped, sourceEntityId);
                return;
            } catch (ReflectiveOperationException ignored) {
                // fall through to 2-arg
            }
        }
        if (SEND_SIMPLE != null) {
            try {
                SEND_SIMPLE.invoke(viewer, location, clamped);
            } catch (ReflectiveOperationException ignored) {
                // progress FX is best-effort
            }
        }
    }

    private static @Nullable Method resolveWithSourceId() {
        try {
            return Player.class.getMethod("sendBlockDamage", Location.class, float.class, int.class);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    private static @Nullable Method resolveSimple() {
        try {
            return Player.class.getMethod("sendBlockDamage", Location.class, float.class);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }
}
