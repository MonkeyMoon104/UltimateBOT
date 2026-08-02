package com.monkey.ultimatebot.bot.ai.controllers.enderpearl.helper.inter;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public interface IPositionCalculator {

    Vec3 calculateEmergencyEscape(Player bot, Player target);

    Vec3 calculateMeleeDisengage(Player bot, Player target);

    Vec3 calculateLowGroundPosition(Player bot, Player target);

    Vec3 calculateAnchorPosition(Player bot, Player target);

    Vec3 calculateAggressiveApproach(Player bot, Player target, Vec3 predictedTargetMovement);

    Vec3 calculateStandardEscape(Player bot, Player target);
}
