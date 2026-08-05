package com.monkey.ultimatebot.extension.runtime;

import com.monkey.ultimatebot.api.extension.control.BotControl;
import com.monkey.ultimatebot.bot.ai.controllers.attack.BotAttackController;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.bot.ai.controllers.movement.BotMovementController;
import com.monkey.ultimatebot.bot.ai.controllers.rotation.BotRotationController;
import java.util.Objects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public final class CoreBotControl implements BotControl {
    private final Player bot;
    private final BotMovementController movement;
    private final BotRotationController rotation;
    private final BotAttackController attack;
    private final BotInventoryController inventory;

    public CoreBotControl(
            Player bot,
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
        rotation.updateRotation(nms(target));
    }

    @Override
    public void moveTo(Location destination) {
        Location checked = Objects.requireNonNull(destination, "destination");
        World destinationWorld = Objects.requireNonNull(checked.getWorld(), "destination.world");
        if (!destinationWorld.getUID().equals(bot.level().getWorld().getUID())) {
            throw new IllegalArgumentException("destination must be in the bot world");
        }
        movement.moveToPosition(new Vec3(checked.getX(), checked.getY(), checked.getZ()));
    }

    @Override
    public void moveTowards(org.bukkit.entity.LivingEntity target, double desiredDistance) {
        requireDistance(desiredDistance);
        LivingEntity handle = nms(target);
        Vec3 delta = handle.position().subtract(bot.position());
        if (delta.horizontalDistanceSqr() <= desiredDistance * desiredDistance) {
            movement.stopMovement();
            return;
        }
        movement.moveToPosition(handle.position());
    }

    @Override
    public void moveAwayFrom(org.bukkit.entity.LivingEntity target, double desiredDistance) {
        requireDistance(desiredDistance);
        LivingEntity handle = nms(target);
        Vec3 away = bot.position().subtract(handle.position());
        if (away.horizontalDistanceSqr() < 1.0E-6D) {
            away = new Vec3(1.0D, 0.0D, 0.0D);
        }
        movement.moveToPosition(bot.position().add(away.normalize().scale(desiredDistance)));
    }

    @Override
    public void stopMovement() {
        movement.stopMovement();
    }

    @Override
    public void attack(org.bukkit.entity.LivingEntity target) {
        attack.handleAttack(nms(target));
    }

    @Override
    public void swingMainHand() {
        bot.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
    }

    @Override
    public void switchToSlot(int slot) {
        validateSlot(slot);
        inventory.switchToSlot(slot);
    }

    @Override
    public void setInventoryItem(int slot, ItemStack item) {
        validateSlot(slot);
        inventory.setItem(slot, CraftItemStack.asNMSCopy(Objects.requireNonNull(item, "item")));
    }

    @Override
    public ItemStack inventoryItem(int slot) {
        validateSlot(slot);
        return CraftItemStack.asBukkitCopy(inventory.getItem(slot));
    }

    private static LivingEntity nms(org.bukkit.entity.LivingEntity entity) {
        if (!(Objects.requireNonNull(entity, "entity") instanceof CraftLivingEntity craftEntity)) {
            throw new IllegalArgumentException("entity does not expose a CraftBukkit handle");
        }
        return craftEntity.getHandle();
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
