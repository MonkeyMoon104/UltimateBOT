package com.monkey.ultimatebot.bot.ai.controllers.attack.helper;

import com.monkey.ultimatebot.bot.ai.controllers.attack.helper.inter.IAttackExecutor;
import com.monkey.ultimatebot.bot.ai.controllers.attack.helper.inter.IAttackStrategy;
import com.monkey.ultimatebot.bot.ai.controllers.attack.helper.inter.ICooldownManager;
import com.monkey.ultimatebot.bot.ai.controllers.attack.helper.inter.IJumpAttackManager;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import org.bukkit.entity.LivingEntity;

public class AttackStrategy implements IAttackStrategy {

    private final IJumpAttackManager jumpAttackManager;
    private final IAttackExecutor attackExecutor;
    private final ICooldownManager cooldownManager;

    public AttackStrategy(
            IJumpAttackManager jumpAttackManager, IAttackExecutor attackExecutor, ICooldownManager cooldownManager) {
        this.jumpAttackManager = jumpAttackManager;
        this.attackExecutor = attackExecutor;
        this.cooldownManager = cooldownManager;
    }

    @Override
    public void executeAttack(ITrainingBot bot, LivingEntity target) {
        if (jumpAttackManager.isInJumpAttack()) {
            jumpAttackManager.handleJumpAttack(bot, target);
            return;
        }

        if (bot.isOnGround()) {
            jumpAttackManager.initiateJumpAttack(bot);
        } else if (!bot.isOnGround()) {
            bot.swingMainHand();
            attackExecutor.performCriticalAttack(bot, target);
            cooldownManager.setRandomCooldown(20, 11);
        }
    }
}
