package com.monkey.ultimatebot.bot.ai.controllers.totem.helper.interf;

import com.monkey.ultimatebot.common.model.EquipmentSlotKind;
import org.bukkit.inventory.ItemStack;

public interface ITotemInventoryManager {
    boolean hasTotemInSlot(ItemStack itemStack);

    void equipTotem(EquipmentSlotKind slot);

    void removeTotem(EquipmentSlotKind slot);

    int getEquippedTotemCount();

    void forceEquipTotems(int count);
}
