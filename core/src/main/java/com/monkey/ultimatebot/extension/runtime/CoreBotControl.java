package com.monkey.ultimatebot.extension.runtime;

import com.monkey.ultimatebot.api.extension.control.BotControl;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.controllers.attack.BotAttackController;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.bot.ai.controllers.movement.BotMovementController;
import com.monkey.ultimatebot.bot.ai.controllers.rotation.BotRotationController;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public final class CoreBotControl implements BotControl {
    private final ITrainingBot bot;
    private final BotMovementController movement;
    private final BotRotationController rotation;
    private final BotAttackController attack;
    private final BotInventoryController inventory;

    public CoreBotControl(
            ITrainingBot bot,
            BotMovementController movement,
            BotRotationController rotation,
            BotAttackController attack,
            BotInventoryController inventory) {
        this.bot = Objects.requireNonNull(bot, "bot");
        this.movement = Objects.requireNonNull(movement, "movement");
        this.rotation = Objects.requireNonNull(rotation, "rotation");
        this.attack = Objects.requireNonNull(attack, "attack");
        this.inventory = Objects.requireNonNull(inventory, "inventory");
    }

    @Override
    public void lookAt(org.bukkit.entity.LivingEntity target) {
        rotation.updateRotation(target);
    }

    @Override
    public void moveTo(Location destination) {
        Location checked = Objects.requireNonNull(destination, "destination");
        World destinationWorld = Objects.requireNonNull(checked.getWorld(), "destination.world");
        if (!destinationWorld.getUID().equals(bot.getWorld().getUID())) {
            throw new IllegalArgumentException("destination must be in the bot world");
        }
        movement.moveToPosition(checked.toVector());
    }

    @Override
    public void moveTowards(org.bukkit.entity.LivingEntity target, double desiredDistance) {
        requireDistance(desiredDistance);
        Vector delta = target.getLocation().toVector().subtract(bot.bukkitPosition());
        delta.setY(0.0D);
        if (delta.lengthSquared() <= desiredDistance * desiredDistance) {
            movement.stopMovement();
            return;
        }
        movement.moveToPosition(target.getLocation().toVector());
    }

    @Override
    public void moveAwayFrom(org.bukkit.entity.LivingEntity target, double desiredDistance) {
        requireDistance(desiredDistance);
        Vector away = bot.bukkitPosition().subtract(target.getLocation().toVector());
        away.setY(0.0D);
        if (away.lengthSquared() < 1.0E-6D) {
            away = new Vector(1.0D, 0.0D, 0.0D);
        }
        movement.moveToPosition(bot.bukkitPosition().add(away.normalize().multiply(desiredDistance)));
    }

    @Override
    public void stopMovement() {
        movement.stopMovement();
    }

    @Override
    public void attack(org.bukkit.entity.LivingEntity target) {
        attack.handleAttack(target);
    }

    @Override
    public void swingMainHand() {
        bot.swingMainHand();
    }

    @Override
    public void switchToSlot(int slot) {
        validateSlot(slot);
        inventory.switchToSlot(slot);
    }

    @Override
    public void setInventoryItem(int slot, ItemStack item) {
        validateSlot(slot);
        inventory.setItem(slot, Objects.requireNonNull(item, "item"));
    }

    @Override
    public ItemStack inventoryItem(int slot) {
        validateSlot(slot);
        return inventory.getItem(slot).clone();
    }

    private static void validateSlot(int slot) {
        if (slot < 0 || slot > 8) {
            throw new IllegalArgumentException("slot must be between 0 and 8");
        }
    }

    private static void requireDistance(double distance) {
        if (!Double.isFinite(distance) || distance < 0.0D) {
            throw new IllegalArgumentException("desiredDistance must be finite and non-negative");
        }
    }
}
