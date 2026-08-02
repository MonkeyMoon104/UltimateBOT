package com.monkey.ultimatebot.combat.mode;

import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

final class ModeInventorySession implements AutoCloseable {
    private static final int HOTBAR_SIZE = 9;

    private final BotInventoryController inventory;
    private final ItemStack[] originalItems = new ItemStack[HOTBAR_SIZE];
    private final Map<EquipmentSlot, ItemStack> originalEquipment = new java.util.EnumMap<>(EquipmentSlot.class);
    private boolean captured;

    ModeInventorySession(BotInventoryController inventory) {
        this.inventory = Objects.requireNonNull(inventory, "inventory");
    }

    void apply(ModeKit kit) {
        Objects.requireNonNull(kit, "kit");
        capture();
        restoreOriginalItems();
        restoreOriginalEquipment();
        for (Map.Entry<Integer, ItemStack> entry : kit.slots().entrySet()) {
            inventory.setItem(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<EquipmentSlot, ItemStack> entry : kit.equipment().entrySet()) {
            inventory.setEquipment(entry.getKey(), entry.getValue());
        }
        inventory.switchToSlot(BotInventoryController.SWORD_SLOT);
    }

    private void capture() {
        if (captured) {
            return;
        }
        for (int slot = 0; slot < HOTBAR_SIZE; slot++) {
            originalItems[slot] = inventory.getItem(slot).copy();
        }
        for (EquipmentSlot slot : managedEquipmentSlots()) {
            originalEquipment.put(slot, inventory.getEquipment(slot).copy());
        }
        captured = true;
    }

    private void restoreOriginalEquipment() {
        if (!captured) {
            return;
        }
        originalEquipment.forEach(inventory::setEquipment);
    }

    private static EquipmentSlot[] managedEquipmentSlots() {
        return new EquipmentSlot[] {
            EquipmentSlot.OFFHAND, EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
        };
    }

    private void restoreOriginalItems() {
        if (!captured) {
            return;
        }
        for (int slot = 0; slot < HOTBAR_SIZE; slot++) {
            inventory.setItem(slot, originalItems[slot].copy());
        }
    }

    @Override
    public void close() {
        restoreOriginalItems();
        restoreOriginalEquipment();
        Arrays.fill(originalItems, ItemStack.EMPTY);
        originalEquipment.clear();
        captured = false;
    }
}
