package com.monkey.mcbot.bot.ai.controllers.attack.helper;

import com.monkey.mcbot.bot.ai.controllers.attack.helper.inter.IAttackExecutor;
import com.monkey.mcbot.bot.ai.controllers.attack.helper.inter.IAttackStrategy;
import com.monkey.mcbot.bot.ai.controllers.attack.helper.inter.ICooldownManager;
import com.monkey.mcbot.bot.ai.controllers.attack.helper.inter.IJumpAttackManager;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;

public class AttackStrategy implements IAttackStrategy {

    private final IJumpAttackManager jumpAttackManager;
    private final IAttackExecutor attackExecutor;
    private final ICooldownManager cooldownManager;

    public AttackStrategy(IJumpAttackManager jumpAttackManager, IAttackExecutor attackExecutor, ICooldownManager cooldownManager) {
        this.jumpAttackManager = jumpAttackManager;
        this.attackExecutor = attackExecutor;
        this.cooldownManager = cooldownManager;
    }

    @Override
    public void executeAttack(Player bot, LivingEntity target) {
        if (jumpAttackManager.isInJumpAttack()) {
            jumpAttackManager.handleJumpAttack(bot, target);
            return;
        }

        if (bot.onGround()) {
            jumpAttackManager.initiateJumpAttack(bot);
        } else if (!bot.onGround()) {
            bot.swing(InteractionHand.MAIN_HAND);
            attackExecutor.performCriticalAttack(bot, target);
            cooldownManager.setRandomCooldown(20, 11);
        }
    }
}
