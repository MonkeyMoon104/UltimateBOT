package com.monkey.ultimatebot.bot.ai.controllers.totem.helper;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.helper.inter.IEquipmentBroadcaster;
import com.monkey.ultimatebot.bot.ai.controllers.totem.helper.interf.ITotemInventoryManager;
import com.monkey.ultimatebot.compat.ItemStackAccess;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class TotemInventoryManager implements ITotemInventoryManager {
    private final ITrainingBot bot;
    private final IEquipmentBroadcaster equipmentBroadcaster;

    public TotemInventoryManager(ITrainingBot bot, IEquipmentBroadcaster equipmentBroadcaster) {
        this.bot = Objects.requireNonNull(bot, "bot");
        this.equipmentBroadcaster = Objects.requireNonNull(equipmentBroadcaster, "equipmentBroadcaster");
    }

    @Override
    public boolean hasTotemInSlot(ItemStack itemStack) {
        return itemStack != null
                && !ItemStackAccess.isEmpty(itemStack)
                && itemStack.getType() == Material.TOTEM_OF_UNDYING;
    }

    @Override
    public void equipTotem(EquipmentSlot slot) {
        if (updateSlot(slot, new ItemStack(Material.TOTEM_OF_UNDYING))) {
            equipmentBroadcaster.broadcastEquipmentChange(bot);
        }
    }

    @Override
    public void removeTotem(EquipmentSlot slot) {
        if (updateSlot(slot, ItemStackAccess.empty())) {
            equipmentBroadcaster.broadcastEquipmentChange(bot);
        }
    }

    @Override
    public int getEquippedTotemCount() {
        ItemStack offhand = bot.getItem(EquipmentSlot.OFF_HAND);
        ItemStack mainhand = bot.getItem(EquipmentSlot.HAND);

        return (hasTotemInSlot(offhand) ? 1 : 0) + (hasTotemInSlot(mainhand) ? 1 : 0);
    }

    @Override
    public void forceEquipTotems(int count) {
        count = Math.max(0, Math.min(2, count));

        boolean changed =
                updateSlot(EquipmentSlot.OFF_HAND, count >= 1 ? new ItemStack(Material.TOTEM_OF_UNDYING) : ItemStackAccess.empty());
        changed |= updateSlot(
                EquipmentSlot.HAND, count >= 2 ? new ItemStack(Material.TOTEM_OF_UNDYING) : ItemStackAccess.empty());
        if (changed) {
            equipmentBroadcaster.broadcastEquipmentChange(bot);
        }
    }

    private boolean updateSlot(EquipmentSlot slot, ItemStack item) {
        EquipmentSlot checkedSlot = Objects.requireNonNull(slot, "slot");
        ItemStack checkedItem = Objects.requireNonNull(item, "item");
        ItemStack current = bot.getItem(checkedSlot);
        if (current.isSimilar(checkedItem) && current.getAmount() == checkedItem.getAmount()) {
            return false;
        }
        bot.setItem(checkedSlot, checkedItem.clone());
        return true;
    }
}
