package com.monkey.ultimatebot.bot.ai.fakeplayer.v1_16_R2;

import net.minecraft.server.v1_16_R2.EntityMinecartTNT;
import net.minecraft.server.v1_16_R2.World;
import org.jspecify.annotations.NullUnmarked;

/**
 * Vanilla {@code explode(double)} is protected {@code h(double)} on this mapping. Subclass so the
 * 1.16.3 bridge can detonate without reflection or private fuse-field writes.
 */
@NullUnmarked
public final class VersionedTntMinecart extends EntityMinecartTNT {

    public VersionedTntMinecart(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    public void explodeNow() {
        h(1.0D);
    }
}
