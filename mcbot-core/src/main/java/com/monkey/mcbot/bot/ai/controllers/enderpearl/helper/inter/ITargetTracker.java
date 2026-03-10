package com.monkey.mcbot.bot.ai.controllers.enderpearl.helper.inter;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public interface ITargetTracker {

    void updateTargetTracking(Player target);

    Vec3 getPredictedTargetMovement();

    Vec3 getLastTargetPosition();
}