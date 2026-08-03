package com.monkey.ultimatebot.bot.ai.controllers.teleport.helper;

import com.monkey.ultimatebot.bot.ai.controllers.teleport.helper.inter.ITeleportStrategy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class BesideTargetStrategy implements ITeleportStrategy {

    private final double offset;

    public BesideTargetStrategy(double offset) {
        this.offset = offset;
    }

    @Override
    public @Nullable Vec3 findTeleportPosition(Player bot, @Nullable Player target) {
        if (target == null) return null;
        Vec3 pos = target.position();
        return pos.add(offset, 0, 0);
    }
}
