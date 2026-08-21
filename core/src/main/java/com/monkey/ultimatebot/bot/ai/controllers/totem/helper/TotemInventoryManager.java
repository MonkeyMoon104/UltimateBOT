package com.monkey.ultimatebot.bot.ai.controllers.totem.helper;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.helper.inter.IEquipmentBroadcaster;
import com.monkey.ultimatebot.bot.ai.controllers.totem.helper.interf.ITotemInventoryManager;
import com.monkey.ultimatebot.common.model.bot.EquipmentSlotKind;
import com.monkey.ultimatebot.access.item.EquipmentSlotAccess;
import com.monkey.ultimatebot.access.item.ItemStackAccess;
import com.monkey.ultimatebot.utils.material.MaterialCatalog;
import java.util.Objects;
import org.bukkit.Material;
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
                && MaterialCatalog.is(itemStack.getType(), "TOTEM_OF_UNDYING");
    }

    @Override
    public void equipTotem(EquipmentSlotKind slot) {
        if (slot == null) {
            return;
        }
        if (updateSlot(slot, MaterialCatalog.stack("TOTEM_OF_UNDYING", Material.GOLDEN_APPLE))) {
            equipmentBroadcaster.broadcastEquipmentChange(bot);
        }
    }

    @Override
    public void removeTotem(EquipmentSlotKind slot) {
        if (slot == null) {
            return;
        }
        if (updateSlot(slot, ItemStackAccess.empty())) {
            equipmentBroadcaster.broadcastEquipmentChange(bot);
        }
    }

    @Override
    public int getEquippedTotemCount() {
        EquipmentSlotKind offHand = EquipmentSlotAccess.offHand();
        ItemStack offhand = offHand == null ? ItemStackAccess.empty() : bot.getItem(offHand);
        ItemStack mainhand = bot.getItem(EquipmentSlotKind.HAND);

        return (hasTotemInSlot(offhand) ? 1 : 0) + (hasTotemInSlot(mainhand) ? 1 : 0);
    }

    @Override
    public void forceEquipTotems(int count) {
        count = Math.max(0, Math.min(2, count));

        EquipmentSlotKind offHand = EquipmentSlotAccess.offHand();
        boolean changed = false;
        if (offHand != null) {
            changed = updateSlot(
                    offHand,
                    count >= 1
                            ? MaterialCatalog.stack("TOTEM_OF_UNDYING", Material.GOLDEN_APPLE)
                            : ItemStackAccess.empty());
        }
        changed |= updateSlot(
                EquipmentSlotKind.HAND,
                count >= 2
                        ? MaterialCatalog.stack("TOTEM_OF_UNDYING", Material.GOLDEN_APPLE)
                        : ItemStackAccess.empty());
        if (changed) {
            equipmentBroadcaster.broadcastEquipmentChange(bot);
        }
    }

    private boolean updateSlot(EquipmentSlotKind slot, ItemStack item) {
        EquipmentSlotKind checkedSlot = Objects.requireNonNull(slot, "slot");
        ItemStack checkedItem = Objects.requireNonNull(item, "item");
        ItemStack current = bot.getItem(checkedSlot);
        if (current.isSimilar(checkedItem) && current.getAmount() == checkedItem.getAmount()) {
            return false;
        }
        bot.setItem(checkedSlot, checkedItem.clone());
        return true;
    }
}
