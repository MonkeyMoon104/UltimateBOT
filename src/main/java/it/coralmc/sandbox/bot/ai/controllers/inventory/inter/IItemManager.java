package it.coralmc.sandbox.bot.ai.controllers.inventory.inter;

import net.minecraft.world.item.ItemStack;

public interface IItemManager {

    void addEnderpearls(int count);

    void updateTotemSlot(ItemStack totemStack);

    void onItemUsed(int slot);
}