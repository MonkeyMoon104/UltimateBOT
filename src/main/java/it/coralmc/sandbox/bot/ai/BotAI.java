package it.coralmc.sandbox.bot.ai;

import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.bot.ai.controllers.*;
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
    private final BotAttackController attackController;
    private final BotInventoryController inventoryController;
    private final BotEnderpearlController enderpearlController;
    private final BotCPVPController cpvpController;

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
        this.attackController = new BotAttackController(bot);
        this.inventoryController = new BotInventoryController(bot);
        this.enderpearlController = new BotEnderpearlController(bot, inventoryController, rotationController);
        this.cpvpController = new BotCPVPController(bot, inventoryController, rotationController);
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
        }

        Player target = ((CraftPlayer) targetBukkitPlayer).getHandle();
        inventoryController.tick();
        if (!enderpearlController.isThrowingPearl() || !cpvpController.isDoingCrystalAction()) {
            rotationController.updateRotation(target);
        }

        double dist = bot.distanceTo(target);

        if (Math.abs(dist - targetDistance) <= 0.3) {
            movementController.stopMovement();
        } else if (dist < targetDistance) {
            movementController.moveAwayFrom(target, targetDistance);
        } else if (dist > targetDistance) {
            movementController.moveTowards(target, targetDistance);
        }

        if (((TrainingBot) bot).isCombat()) {
            enderpearlController.tick();
            cpvpController.tick(target);

            boolean pearlThrown = enderpearlController.tryUseEnderpearl(target);
            if (!pearlThrown && enderpearlController.canUseEnderpearl()) {
                cpvpController.getBestObsidianForPearl(target).ifPresent(obsidianPos -> {
                    if (enderpearlController.tryPearlToObsidianSide(obsidianPos, target)) {
                        return;
                    }
                });
            }

            if (!inventoryController.isHoldingSword() && dist <= 4.0) {
                inventoryController.switchToSword();
            }

            if (dist <= 3.0 && ((TrainingBot) bot).isFollow()) {
                if (!inventoryController.isHoldingSword()) {
                    inventoryController.switchToSword();
                }
                attackController.handleAttack(target);
            }
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

    public BotInventoryController getInventoryController() {
        return inventoryController;
    }

    public BotEnderpearlController getEnderpearlController() {
        return enderpearlController;
    }

    public BotCPVPController getCPVPController() {
        return cpvpController;
    }
}