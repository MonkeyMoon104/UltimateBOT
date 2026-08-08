package com.monkey.ultimatebot.bot.ai.controllers.attack.helper.inter;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import org.bukkit.entity.LivingEntity;

public interface IJumpAttackManager {

    void handleJumpAttack(ITrainingBot bot, LivingEntity target);

    void initiateJumpAttack(ITrainingBot bot);

    boolean isInJumpAttack();

    void resetJumpState(ITrainingBot bot);
}
