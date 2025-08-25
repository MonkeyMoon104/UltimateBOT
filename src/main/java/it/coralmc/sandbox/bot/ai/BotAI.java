package it.coralmc.sandbox.bot.ai;

import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.bot.ai.controllers.BotMovementController;
import it.coralmc.sandbox.bot.ai.controllers.BotRotationController;
import it.coralmc.sandbox.bot.ai.controllers.BotTotemController;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.entity.CraftPlayer;

import java.util.Random;

public class BotAI {

    private final Player bot;
    private final Level level;
    private final BotMovementController movementController;
    private final BotRotationController rotationController;
    private final BotTotemController totemController;

    private int knockbackCooldown = 0;
    private int attackCooldown = 0;
    private final Random random = new Random();

    private boolean isJumping = false;
    private int jumpTicks = 0;
    private static final int JUMP_ATTACK_DELAY = 7;

    public BotAI(Player bot, SandboxTraining plugin) {
        this.bot = bot;
        this.level = bot.level();

        this.movementController = new BotMovementController(bot, level);
        this.rotationController = new BotRotationController(bot);
        this.totemController = new BotTotemController(bot, plugin);
    }

    public void setKnockbackCooldown(int ticks) {
        this.knockbackCooldown = ticks;
    }

    public void tick(org.bukkit.entity.Player targetBukkitPlayer) {
        tick(targetBukkitPlayer, 1.5);
    }

    public void tick(org.bukkit.entity.Player targetBukkitPlayer, double targetDistance) {
        if (targetBukkitPlayer == null || targetBukkitPlayer.isDead()) return;

        if (knockbackCooldown > 0) {
            knockbackCooldown--;
            return;
        }

        Player target = ((CraftPlayer) targetBukkitPlayer).getHandle();
        rotationController.updateRotation(target);

        double dist = bot.distanceTo(target);
        if (Math.abs(dist - targetDistance) <= 0.3) {
            movementController.stopMovement();
        } else if (dist < targetDistance) {
            movementController.moveAwayFrom(target, targetDistance);
        } else if (dist > targetDistance) {
            movementController.moveTowards(target, targetDistance);
        }

        if (dist <= 3.0 && ((TrainingBot) bot).isFollow()) {
            handleAttack(target);
        }
    }

    private void handleAttack(Player target) {
        if (attackCooldown > 0) {
            attackCooldown--;
            return;
        }

        if (isJumping) {
            jumpTicks++;

            if (jumpTicks >= JUMP_ATTACK_DELAY && !bot.onGround()) {
                bot.swing(InteractionHand.MAIN_HAND);
                bot.attack(target);

                isJumping = false;
                jumpTicks = 0;
                bot.setSprinting(true);
                attackCooldown = 20 + random.nextInt(11);
            }

            if (bot.onGround() && jumpTicks > 10) {
                isJumping = false;
                jumpTicks = 0;
                bot.setSprinting(true);
                attackCooldown = 10;
            }

            return;
        }

        if (bot.onGround()) {
            bot.setSprinting(true);
            bot.setDeltaMovement(bot.getDeltaMovement().x, 0.42, bot.getDeltaMovement().z);
            isJumping = true;
            jumpTicks = 0;
        }
        else if (!bot.onGround()) {
            bot.swing(InteractionHand.MAIN_HAND);
            bot.attack(target);
            attackCooldown = 20 + random.nextInt(11);
        }
    }

    public void manageTotem() {
        totemController.manageTotem();
    }

    public BotMovementController getMovementController() {
        return movementController;
    }

    public BotRotationController getRotationController() {
        return rotationController;
    }

    public BotTotemController getTotemController() {
        return totemController;
    }
}