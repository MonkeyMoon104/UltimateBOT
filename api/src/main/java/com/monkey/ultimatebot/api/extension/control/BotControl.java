package com.monkey.ultimatebot.api.extension.control;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

/** Stable, server-thread-only controls available to custom combat sessions and brains. */
public interface BotControl {
    void lookAt(LivingEntity target);

    void moveTo(Location destination);

    void moveTowards(LivingEntity target, double desiredDistance);

    void moveAwayFrom(LivingEntity target, double desiredDistance);

    void stopMovement();

    void attack(LivingEntity target);

    void swingMainHand();

    void switchToSlot(int slot);

    void setInventoryItem(int slot, ItemStack item);

    ItemStack inventoryItem(int slot);
}
