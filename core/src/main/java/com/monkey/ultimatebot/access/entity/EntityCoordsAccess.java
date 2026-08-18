package com.monkey.ultimatebot.access.entity;

import org.bukkit.entity.Entity;

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
