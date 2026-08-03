package com.monkey.ultimatebot.bot.ai.controllers.totem.helper;

import com.monkey.ultimatebot.bot.ai.controllers.inventory.helper.inter.IEquipmentBroadcaster;
import com.monkey.ultimatebot.bot.ai.controllers.totem.helper.interf.ITotemInventoryManager;
import java.util.Objects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class TotemInventoryManager implements ITotemInventoryManager {
    private final Player bot;
    private final IEquipmentBroadcaster equipmentBroadcaster;

    public TotemInventoryManager(Player bot, IEquipmentBroadcaster equipmentBroadcaster) {
        this.bot = Objects.requireNonNull(bot, "bot");
        this.equipmentBroadcaster = Objects.requireNonNull(equipmentBroadcaster, "equipmentBroadcaster");
    }

    @Override
    public boolean hasTotemInSlot(ItemStack itemStack) {
        return itemStack != null && !itemStack.isEmpty() && Items.TOTEM_OF_UNDYING.equals(itemStack.getItem());
    }

    @Override
    public void equipTotem(EquipmentSlot slot) {
        if (updateSlot(slot, new ItemStack(Items.TOTEM_OF_UNDYING))) {
            equipmentBroadcaster.broadcastEquipmentChange(bot);
        }
    }

    @Override
    public void removeTotem(EquipmentSlot slot) {
        if (updateSlot(slot, ItemStack.EMPTY)) {
            equipmentBroadcaster.broadcastEquipmentChange(bot);
        }
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

        boolean changed =
                updateSlot(EquipmentSlot.OFFHAND, count >= 1 ? new ItemStack(Items.TOTEM_OF_UNDYING) : ItemStack.EMPTY);
        changed |= updateSlot(
                EquipmentSlot.MAINHAND, count >= 2 ? new ItemStack(Items.TOTEM_OF_UNDYING) : ItemStack.EMPTY);
        if (changed) {
            equipmentBroadcaster.broadcastEquipmentChange(bot);
        }
    }

    private boolean updateSlot(EquipmentSlot slot, ItemStack item) {
        EquipmentSlot checkedSlot = Objects.requireNonNull(slot, "slot");
        ItemStack checkedItem = Objects.requireNonNull(item, "item");
        if (ItemStack.matches(bot.getItemBySlot(checkedSlot), checkedItem)) {
            return false;
        }
        bot.setItemSlot(checkedSlot, checkedItem.copy());
        return true;
    }
}
