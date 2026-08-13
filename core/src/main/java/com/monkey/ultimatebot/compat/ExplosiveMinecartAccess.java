package com.monkey.ultimatebot.compat;

import java.util.Objects;
import org.bukkit.entity.minecart.ExplosiveMinecart;

/**
 * Cross-version TNT-minecart fuse control.
 *
 * <p>Modern Paper declares {@code ExplosiveMinecart#setFuseTicks}; older API jars (e.g. 1.19.1) do
 * not, even when Craft implements it. Dual-path so call sites never hard-link the interface method.
 */
public final class ExplosiveMinecartAccess {

    private ExplosiveMinecartAccess() {}

    /** {@code -1} keeps the cart inert; {@code 1} detonates on the next tick. */
    public static void setFuseTicks(ExplosiveMinecart cart, int ticks) {
        ExplosiveMinecartOpsLookup.get().setFuseTicks(Objects.requireNonNull(cart, "cart"), ticks);
    }
}
