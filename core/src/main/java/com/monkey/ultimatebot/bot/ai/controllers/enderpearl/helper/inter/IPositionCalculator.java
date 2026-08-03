package com.monkey.ultimatebot.bot.ai.controllers.enderpearl.helper.inter;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public interface IPositionCalculator {

    @Nullable Vec3 calculateEmergencyEscape(Player bot, Player target);

    @Nullable Vec3 calculateMeleeDisengage(Player bot, Player target);

    @Nullable Vec3 calculateLowGroundPosition(Player bot, Player target);

    @Nullable Vec3 calculateAnchorPosition(Player bot, Player target);

    @Nullable Vec3 calculateAggressiveApproach(Player bot, Player target, @Nullable Vec3 predictedTargetMovement);

    @Nullable Vec3 calculateStandardEscape(Player bot, Player target);
}
