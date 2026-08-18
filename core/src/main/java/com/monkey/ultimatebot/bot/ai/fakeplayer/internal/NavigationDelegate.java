package com.monkey.ultimatebot.bot.ai.fakeplayer.internal;

import java.util.function.Supplier;
import net.minecraft.world.entity.player.Player;
import org.bukkit.Location;

public final class NavigationDelegate {
    private final Supplier<Location> locationSupplier;
    private final Supplier<Player> nativeHandleSupplier;

    public NavigationDelegate(Supplier<Location> locationSupplier, Supplier<Player> nativeHandleSupplier) {
        this.locationSupplier = locationSupplier;
        this.nativeHandleSupplier = nativeHandleSupplier;
    }

    public Location getCompassTarget() {
        return locationSupplier.get();
    }

    public void setRotation(float yaw, float pitch) {
        Player handle = nativeHandleSupplier.get();
        handle.setYRot(yaw);
        handle.setXRot(pitch);
        handle.setYHeadRot(yaw);
        handle.yBodyRot = yaw;
    }
}
