package com.monkey.ultimatebot.bot.ai.controllers.enderpearl.helper.inter;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

public interface IPositionCalculator {

    @Nullable Vector calculateEmergencyEscape(Player bot, Player target);

    @Nullable Vector calculateMeleeDisengage(Player bot, Player target);

    @Nullable Vector calculateLowGroundPosition(Player bot, Player target);

    @Nullable Vector calculateAnchorPosition(Player bot, Player target);

    @Nullable Vector calculateAggressiveApproach(Player bot, Player target, @Nullable Vector predictedTargetMovement);

    @Nullable Vector calculateStandardEscape(Player bot, Player target);
}
