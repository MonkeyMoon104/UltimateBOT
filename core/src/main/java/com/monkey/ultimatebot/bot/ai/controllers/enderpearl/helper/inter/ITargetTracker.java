package com.monkey.ultimatebot.bot.ai.controllers.enderpearl.helper.inter;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public interface ITargetTracker {

    void updateTargetTracking(Player target);

    Vec3 getPredictedTargetMovement();

    @Nullable Vec3 getLastTargetPosition();
}
