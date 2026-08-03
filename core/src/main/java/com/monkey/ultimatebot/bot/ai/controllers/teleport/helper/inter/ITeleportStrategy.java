package com.monkey.ultimatebot.bot.ai.controllers.teleport.helper.inter;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public interface ITeleportStrategy {
    @Nullable Vec3 findTeleportPosition(Player bot, @Nullable Player target);
}
