package com.monkey.ultimatebot.bot.ai.controllers.inventory.helper.inter;

import org.bukkit.inventory.ItemStack;

public interface ISlotManager {

    void switchToSlot(int slot);

    void setItem(int slot, ItemStack item);

    ItemStack getItem(int slot);

    int getCurrentSlot();

    ItemStack getCurrentItem();

    void switchToSword();

    void switchToEnderpearl();

    void switchToTotem();

    void switchToCrystal();

    void switchToObs();

    void switchToAnchor();

    void switchToGlow();

    void switchToGoldenApple();

    void switchToEmptySlot();
}
