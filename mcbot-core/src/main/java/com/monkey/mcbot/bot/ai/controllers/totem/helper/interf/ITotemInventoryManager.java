package com.monkey.mcbot.bot.ai.controllers.totem.helper.interf;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public interface ITotemInventoryManager {
    boolean hasTotemInSlot(ItemStack itemStack);

    void equipTotem(EquipmentSlot slot);

    void removeTotem(EquipmentSlot slot);

    int getEquippedTotemCount();

    void forceEquipTotems(int count);
}
