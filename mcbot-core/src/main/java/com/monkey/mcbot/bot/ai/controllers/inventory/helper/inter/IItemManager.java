package com.monkey.mcbot.bot.ai.controllers.inventory.helper.inter;

import net.minecraft.world.item.ItemStack;

public interface IItemManager {

    void addEnderpearls(int count);

    void updateTotemSlot(ItemStack totemStack);

    void onItemUsed(int slot);
}
