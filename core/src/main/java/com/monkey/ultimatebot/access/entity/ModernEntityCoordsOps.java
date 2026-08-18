package com.monkey.ultimatebot.access.entity;

import java.util.Objects;
import org.bukkit.entity.Entity;

final class ModernEntityCoordsOps implements EntityCoordsOps {
    @Override
    public double getX(Entity entity) {
        return Objects.requireNonNull(entity, "entity").getX();
    }

    @Override
    public double getY(Entity entity) {
        return Objects.requireNonNull(entity, "entity").getY();
    }

    @Override
    public double getZ(Entity entity) {
        return Objects.requireNonNull(entity, "entity").getZ();
    }
}
