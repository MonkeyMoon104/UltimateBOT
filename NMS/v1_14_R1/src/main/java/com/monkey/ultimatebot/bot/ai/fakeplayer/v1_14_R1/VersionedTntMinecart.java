package com.monkey.ultimatebot.bot.ai.fakeplayer.v1_14_R1;

import net.minecraft.server.v1_14_R1.EntityMinecartTNT;
import net.minecraft.server.v1_14_R1.World;
import org.jspecify.annotations.NullUnmarked;

/**
 * Vanilla {@code explode(double)} is protected {@code c(double)} on this mapping (1.15 uses
 * {@code h(double)}). Subclass so the 1.14.4 bridge can detonate without reflection.
 */
@NullUnmarked
public final class VersionedTntMinecart extends EntityMinecartTNT {

    public VersionedTntMinecart(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    public void explodeNow() {
        c(1.0D);
    }
}
