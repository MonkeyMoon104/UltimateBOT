package com.monkey.ultimatebot.bot.ai.controllers.teleport.helper.inter;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public interface ITeleportStrategy {
    Vec3 findTeleportPosition(Player bot, Player target);
}
