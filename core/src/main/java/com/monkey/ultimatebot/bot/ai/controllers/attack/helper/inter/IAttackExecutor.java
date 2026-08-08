package com.monkey.ultimatebot.bot.ai.controllers.attack.helper.inter;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import org.bukkit.entity.LivingEntity;

public interface IAttackExecutor {

    void performCriticalAttack(ITrainingBot bot, LivingEntity target);

    void performNormalAttack(ITrainingBot bot, LivingEntity target);
}
