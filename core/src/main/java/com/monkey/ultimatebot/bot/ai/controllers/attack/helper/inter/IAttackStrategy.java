package com.monkey.ultimatebot.bot.ai.controllers.attack.helper.inter;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import org.bukkit.entity.LivingEntity;

public interface IAttackStrategy {

    void executeAttack(ITrainingBot bot, LivingEntity target);
}
