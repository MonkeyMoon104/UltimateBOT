package com.monkey.ultimatebot.bot.ai.controllers.rotation.helper.inter;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public interface IRotationCalculator {

    float[] calculateRotationToTarget(Player bot, LivingEntity target);

    float[] calculateRotationToPosition(Player bot, double x, double y, double z);

    float[] calculateRotationToPosition(Player bot, Vec3 targetPos);
}
