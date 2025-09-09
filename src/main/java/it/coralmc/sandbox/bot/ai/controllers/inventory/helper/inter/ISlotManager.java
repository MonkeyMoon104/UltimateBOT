package it.coralmc.sandbox.bot.ai.controllers.inventory.helper.inter;

import net.minecraft.world.item.ItemStack;

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