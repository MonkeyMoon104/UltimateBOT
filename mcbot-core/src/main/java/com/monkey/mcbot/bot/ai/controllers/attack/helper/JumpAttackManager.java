package com.monkey.mcbot.bot.ai.controllers.attack.helper;

import com.monkey.mcbot.bot.ai.controllers.attack.helper.inter.IAttackExecutor;
import com.monkey.mcbot.bot.ai.controllers.attack.helper.inter.ICooldownManager;
import com.monkey.mcbot.bot.ai.controllers.attack.helper.inter.IJumpAttackManager;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;

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
    public void handleJumpAttack(Player bot, LivingEntity target) {
        jumpTicks++;

        if (jumpTicks >= JUMP_ATTACK_DELAY && !bot.onGround()) {
            bot.swing(InteractionHand.MAIN_HAND);
            attackExecutor.performCriticalAttack(bot, target);

            isJumping = false;
            jumpTicks = 0;
            bot.setSprinting(false);
            cooldownManager.setRandomCooldown(20, 11);
        }

        if (bot.onGround() && jumpTicks > 10) {
            isJumping = false;
            jumpTicks = 0;
            bot.setSprinting(false);
            cooldownManager.setCooldown(10);
        }
    }

    @Override
    public void initiateJumpAttack(Player bot) {
        bot.setSprinting(true);
        bot.setDeltaMovement(bot.getDeltaMovement().x, 0.42, bot.getDeltaMovement().z);
        isJumping = true;
        jumpTicks = 0;
    }

    @Override
    public boolean isInJumpAttack() {
        return isJumping;
    }

    @Override
    public void resetJumpState(Player bot) {
        isJumping = false;
        jumpTicks = 0;
        bot.setSprinting(false);
    }
}
