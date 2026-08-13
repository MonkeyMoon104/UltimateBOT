package com.monkey.ultimatebot.compat;

import org.bukkit.entity.Entity;

/**
 * Cross-version entity coordinates.
 *
 * <p>Dual-path like ItemStack/trim: modern Paper uses {@link Entity#getX()}/{@link
 * Entity#getY()}/{@link Entity#getZ()}; older servers (e.g. Paper 1.20 early builds) use {@link
 * Entity#getLocation()}. Never hard-link modern methods from shared call sites.
 */
public final class EntityCoordsAccess {

    private EntityCoordsAccess() {}

    public static double getX(Entity entity) {
        return EntityCoordsOpsLookup.get().getX(entity);
    }

    public static double getY(Entity entity) {
        return EntityCoordsOpsLookup.get().getY(entity);
    }

    public static double getZ(Entity entity) {
        return EntityCoordsOpsLookup.get().getZ(entity);
    }
}
