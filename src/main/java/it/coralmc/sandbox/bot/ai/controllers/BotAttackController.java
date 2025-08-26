package it.coralmc.sandbox.bot.ai.controllers;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

import java.util.Random;

public class BotAttackController {

    private final Player bot;
    private final Random random = new Random();

    private int attackCooldown = 0;
    private boolean isJumping = false;
    private int jumpTicks = 0;
    private static final int JUMP_ATTACK_DELAY = 7;

    public BotAttackController(Player bot) {
        this.bot = bot;
    }

    public void handleAttack(Player target) {
        if (attackCooldown > 0) {
            attackCooldown--;
            return;
        }

        if (isJumping) {
            jumpTicks++;

            if (jumpTicks >= JUMP_ATTACK_DELAY && !bot.onGround()) {
                bot.swing(InteractionHand.MAIN_HAND);
                performCriticalAttack(target);

                isJumping = false;
                jumpTicks = 0;
                bot.setSprinting(false);
                attackCooldown = 20 + random.nextInt(11);
            }

            if (bot.onGround() && jumpTicks > 10) {
                isJumping = false;
                jumpTicks = 0;
                bot.setSprinting(false);
                attackCooldown = 10;
            }

            return;
        }

        if (bot.onGround()) {
            bot.setSprinting(true);
            bot.setDeltaMovement(bot.getDeltaMovement().x, 0.42, bot.getDeltaMovement().z);
            isJumping = true;
            jumpTicks = 0;
        } else if (!bot.onGround()) {
            bot.swing(InteractionHand.MAIN_HAND);
            performCriticalAttack(target);
            attackCooldown = 20 + random.nextInt(11);
        }
    }

    private void performCriticalAttack(Player target) {
        try {
            float baseDamage = (float) bot.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
            float criticalDamage = baseDamage * 1.5f;

            net.minecraft.server.level.ServerLevel serverLevel = (net.minecraft.server.level.ServerLevel) bot.level();

            target.hurtServer(serverLevel, bot.damageSources().playerAttack(bot), criticalDamage);

            bot.level().broadcastEntityEvent(target, (byte) 4);

            bot.playSound(net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_CRIT, 1.0f, 1.0f);

        } catch (Exception e) {
            bot.attack(target);
        }
    }

    public void performNormalAttack(Player target) {
        bot.swing(InteractionHand.MAIN_HAND);
        bot.attack(target);
        attackCooldown = 20 + random.nextInt(11);
    }

    public boolean canAttack() {
        return attackCooldown <= 0;
    }

    public void setAttackCooldown(int cooldown) {
        this.attackCooldown = cooldown;
    }

    public int getAttackCooldown() {
        return attackCooldown;
    }

    public boolean isInJumpAttack() {
        return isJumping;
    }
}