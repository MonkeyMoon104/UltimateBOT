package it.coralmc.sandbox.bot.ai;

import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.bot.ai.controllers.BotMovementController;
import it.coralmc.sandbox.bot.ai.controllers.BotRotationController;
import it.coralmc.sandbox.bot.ai.controllers.BotTotemController;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.entity.CraftPlayer;

public class BotAI {

    private final Player bot;
    private final Level level;
    private final BotMovementController movementController;
    private final BotRotationController rotationController;
    private final BotTotemController totemController;
    private int knockbackCooldown = 0;

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
            return;
        }

        if (dist < targetDistance) {
            movementController.moveAwayFrom(target, targetDistance);
        } else if (dist > targetDistance) {
            movementController.moveTowards(target, targetDistance);
        }
    }

    public void manageTotem() {
        totemController.manageTotem();
    }

    public void onTotemUsed() {
        totemController.onTotemUsed();
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