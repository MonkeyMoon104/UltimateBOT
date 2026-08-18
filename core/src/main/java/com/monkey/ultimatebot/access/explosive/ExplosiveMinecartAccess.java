package com.monkey.ultimatebot.access.explosive;

import java.util.Objects;
import org.bukkit.entity.minecart.ExplosiveMinecart;

public final class ExplosiveMinecartAccess {

    private ExplosiveMinecartAccess() {}

    public static void setFuseTicks(ExplosiveMinecart cart, int ticks) {
        ExplosiveMinecartOpsLookup.get().setFuseTicks(Objects.requireNonNull(cart, "cart"), ticks);
    }
}
