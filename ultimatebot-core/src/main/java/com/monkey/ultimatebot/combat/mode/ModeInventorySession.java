package com.monkey.ultimatebot.combat.mode;

import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import net.minecraft.world.item.ItemStack;

final class ModeInventorySession implements AutoCloseable {
    private static final int HOTBAR_SIZE = 9;

    private final BotInventoryController inventory;
    private final ItemStack[] originalItems = new ItemStack[HOTBAR_SIZE];
    private boolean captured;

    ModeInventorySession(BotInventoryController inventory) {
        this.inventory = Objects.requireNonNull(inventory, "inventory");
    }

    void apply(ModeKit kit) {
        Objects.requireNonNull(kit, "kit");
        capture();
        restoreOriginalItems();
        for (Map.Entry<Integer, ItemStack> entry : kit.slots().entrySet()) {
            inventory.setItem(entry.getKey(), entry.getValue());
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
        captured = true;
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
        Arrays.fill(originalItems, ItemStack.EMPTY);
        captured = false;
    }
}
