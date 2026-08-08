package com.monkey.ultimatebot.bot.ai.controllers.totem.helper.interf;

import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public interface ITotemInventoryManager {
    boolean hasTotemInSlot(ItemStack itemStack);

    void equipTotem(EquipmentSlot slot);

    void removeTotem(EquipmentSlot slot);

    int getEquippedTotemCount();

    void forceEquipTotems(int count);
}
