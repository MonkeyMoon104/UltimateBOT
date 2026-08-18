package com.monkey.ultimatebot.access.explosive;

import java.util.Objects;
import org.bukkit.entity.minecart.ExplosiveMinecart;

final class ModernExplosiveMinecartOps implements ExplosiveMinecartOps {
    @Override
    public void setFuseTicks(ExplosiveMinecart cart, int ticks) {
        Objects.requireNonNull(cart, "cart").setFuseTicks(ticks);
    }
}
