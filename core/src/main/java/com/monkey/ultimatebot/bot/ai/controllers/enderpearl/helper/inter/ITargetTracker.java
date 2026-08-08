package com.monkey.ultimatebot.bot.ai.controllers.enderpearl.helper.inter;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

public interface ITargetTracker {

    void updateTargetTracking(Player target);

    Vector getPredictedTargetMovement();

    @Nullable Vector getLastTargetPosition();
}
