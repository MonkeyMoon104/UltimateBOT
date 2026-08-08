package com.monkey.ultimatebot.bot.ai.controllers.inventory.helper.inter;

import org.bukkit.inventory.ItemStack;

public interface IItemManager {

    void addEnderpearls(int count);

    void updateTotemSlot(ItemStack totemStack);

    void onItemUsed(int slot);
}
