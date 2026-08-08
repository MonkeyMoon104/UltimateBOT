package com.monkey.ultimatebot.bot.ai.controllers.attack.helper;

import com.monkey.ultimatebot.bot.ai.controllers.attack.helper.inter.IAttackExecutor;
import com.monkey.ultimatebot.bot.ai.controllers.attack.helper.inter.ICooldownManager;
import com.monkey.ultimatebot.bot.ai.controllers.attack.helper.inter.IJumpAttackManager;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class JumpAttackManager implements IJumpAttackManager {

    private boolean isJumping = false;
    private int jumpTicks = 0;
    private static final int JUMP_ATTACK_DELAY = 7;

    private final IAttackExecutor attackExecutor;
    private final ICooldownManager cooldownManager;

    public JumpAttackManager(IAttackExecutor attackExecutor, ICooldownManager cooldownManager) {
        this.attackExecutor = attackExecutor;
        this.cooldownManager = cooldownManager;
    }

    @Override
    public void handleJumpAttack(ITrainingBot bot, LivingEntity target) {
        jumpTicks++;

        if (jumpTicks >= JUMP_ATTACK_DELAY && !bot.isOnGround()) {
            bot.swingMainHand();
            attackExecutor.performCriticalAttack(bot, target);

            isJumping = false;
            jumpTicks = 0;
            bot.setSprinting(false);
            cooldownManager.setRandomCooldown(20, 11);
        }

        if (bot.isOnGround() && jumpTicks > 10) {
            isJumping = false;
            jumpTicks = 0;
            bot.setSprinting(false);
            cooldownManager.setCooldown(10);
        }
    }

    @Override
    public void initiateJumpAttack(ITrainingBot bot) {
        bot.setSprinting(true);
        Vector velocity = bot.bukkitVelocity();
        velocity.setY(0.42D);
        bot.setBukkitVelocity(velocity);
        isJumping = true;
        jumpTicks = 0;
    }

    @Override
    public boolean isInJumpAttack() {
        return isJumping;
    }

    @Override
    public void resetJumpState(ITrainingBot bot) {
        isJumping = false;
        jumpTicks = 0;
        bot.setSprinting(false);
    }
}
