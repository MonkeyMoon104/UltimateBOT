package com.monkey.ultimatebot.compat;

import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

/**
 * Early Paper/Bukkit without {@code Entity#getX}/{@code getY}/{@code getZ} (e.g. Paper 1.20
 * build 17): read coordinates from {@link Entity#getLocation()}.
 */
final class LegacyEntityCoordsOps implements EntityCoordsOps {
    @Override
    public double getX(Entity entity) {
        return location(entity).getX();
    }

    @Override
    public double getY(Entity entity) {
        return location(entity).getY();
    }

    @Override
    public double getZ(Entity entity) {
        return location(entity).getZ();
    }

    private static Location location(Entity entity) {
        return Objects.requireNonNull(entity, "entity").getLocation();
    }
}
