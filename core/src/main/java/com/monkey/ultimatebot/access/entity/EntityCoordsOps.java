package com.monkey.ultimatebot.access.entity;

import org.bukkit.entity.Entity;

interface EntityCoordsOps {
    double getX(Entity entity);

    double getY(Entity entity);

    double getZ(Entity entity);
}
