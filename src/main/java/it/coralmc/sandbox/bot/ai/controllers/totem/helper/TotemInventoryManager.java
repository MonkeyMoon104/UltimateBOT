package it.coralmc.sandbox.bot.ai.controllers.totem.helper;

import it.coralmc.sandbox.bot.ai.controllers.totem.helper.interf.ITotemInventoryManager;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class TotemInventoryManager implements ITotemInventoryManager {
    private final Player bot;

    public TotemInventoryManager(Player bot) {
        this.bot = bot;
    }

    @Override
    public boolean hasTotemInSlot(ItemStack itemStack) {
        return itemStack != null && !itemStack.isEmpty() && itemStack.is(Items.TOTEM_OF_UNDYING);
    }

    @Override
    public void equipTotem(EquipmentSlot slot) {
        bot.setItemSlot(slot, new ItemStack(Items.TOTEM_OF_UNDYING));
    }

    @Override
    public void removeTotem(EquipmentSlot slot) {
        bot.setItemSlot(slot, ItemStack.EMPTY);
    }

    @Override
    public int getEquippedTotemCount() {
        ItemStack offhand = bot.getItemBySlot(EquipmentSlot.OFFHAND);
        ItemStack mainhand = bot.getItemBySlot(EquipmentSlot.MAINHAND);

        return (hasTotemInSlot(offhand) ? 1 : 0) + (hasTotemInSlot(mainhand) ? 1 : 0);
    }

    @Override
    public void forceEquipTotems(int count) {
        count = Math.max(0, Math.min(2, count));

        removeTotem(EquipmentSlot.OFFHAND);
        removeTotem(EquipmentSlot.MAINHAND);

        if (count >= 1) {
            equipTotem(EquipmentSlot.OFFHAND);
        }
        if (count >= 2) {
            equipTotem(EquipmentSlot.MAINHAND);
        }
    }
}