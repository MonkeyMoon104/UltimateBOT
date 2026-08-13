package com.monkey.ultimatebot.compat;

import org.bukkit.entity.Entity;

/** Versioned entity coordinate reads (Paper {@code Entity#getX} vs {@code getLocation}). */
interface EntityCoordsOps {
    double getX(Entity entity);

    double getY(Entity entity);

    double getZ(Entity entity);
}
