package com.monkey.ultimatebot.bot.ai.fakeplayer.v1_16_R3;

import net.minecraft.server.v1_16_R3.EntityMinecartTNT;
import net.minecraft.server.v1_16_R3.World;
import org.jspecify.annotations.NullUnmarked;

@NullUnmarked
public final class VersionedTntMinecart extends EntityMinecartTNT {

    public VersionedTntMinecart(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    public void explodeNow() {
        h(1.0D);
    }
}
