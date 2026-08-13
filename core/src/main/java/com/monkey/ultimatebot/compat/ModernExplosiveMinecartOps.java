package com.monkey.ultimatebot.compat;

import java.util.Objects;
import org.bukkit.entity.minecart.ExplosiveMinecart;

/** Paper API with {@link ExplosiveMinecart#setFuseTicks(int)}. */
final class ModernExplosiveMinecartOps implements ExplosiveMinecartOps {
    @Override
    public void setFuseTicks(ExplosiveMinecart cart, int ticks) {
        Objects.requireNonNull(cart, "cart").setFuseTicks(ticks);
    }
}
