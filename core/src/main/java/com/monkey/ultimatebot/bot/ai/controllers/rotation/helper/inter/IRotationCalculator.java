package com.monkey.ultimatebot.bot.ai.controllers.rotation.helper.inter;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public interface IRotationCalculator {

    float[] calculateRotationToTarget(ITrainingBot bot, LivingEntity target);

    float[] calculateRotationToPosition(ITrainingBot bot, double x, double y, double z);

    float[] calculateRotationToPosition(ITrainingBot bot, Vector targetPos);
}
