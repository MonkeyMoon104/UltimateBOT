package it.coralmc.sandbox.bot.ai.controllers.totem.interf;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public interface ITotemInventoryManager {
    boolean hasTotemInSlot(ItemStack itemStack);
    void equipTotem(EquipmentSlot slot);
    void removeTotem(EquipmentSlot slot);
    int getEquippedTotemCount();
    void forceEquipTotems(int count);
}