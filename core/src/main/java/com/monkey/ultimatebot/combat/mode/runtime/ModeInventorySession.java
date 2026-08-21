package com.monkey.ultimatebot.combat.mode.runtime;

import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.common.model.bot.EquipmentSlotKind;
import com.monkey.ultimatebot.access.item.EquipmentSlotAccess;
import com.monkey.ultimatebot.utils.equipment.BotEquipmentUtils;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class ModeInventorySession implements AutoCloseable {
    private static final int HOTBAR_SIZE = 9;

    private final BotInventoryController inventory;
    private final ItemStack[] originalItems = new ItemStack[HOTBAR_SIZE];
    private final Map<EquipmentSlotKind, ItemStack> originalEquipment = new EnumMap<>(EquipmentSlotKind.class);
    private int originalSelectedSlot;
    private boolean captured;

    public ModeInventorySession(BotInventoryController inventory) {
        this.inventory = Objects.requireNonNull(inventory, "inventory");
    }

    public void apply(ModeKit kit, BotOptions options) {
        Objects.requireNonNull(kit, "kit");
        Objects.requireNonNull(options, "options");
        capture();
        Map<Integer, ItemStack> hotbar = originalHotbar();

        for (Map.Entry<Integer, ItemStack> entry :
                options.getEquipmentContents().entrySet()) {
            hotbar.put(entry.getKey(), entry.getValue().clone());
        }
        for (Map.Entry<Integer, ItemStack> entry : kit.slots().entrySet()) {
            hotbar.put(entry.getKey(), entry.getValue());
        }
        inventory.applyBukkitHotbarLoadout(hotbar, BotInventoryController.SWORD_SLOT);

        Map<EquipmentSlotKind, ItemStack> equipment = new EnumMap<>(originalEquipment);
        for (Map.Entry<EquipmentSlotKind, ItemStack> entry : kit.equipment().entrySet()) {
            equipment.put(entry.getKey(), entry.getValue());
        }
        applyConfiguredArmor(options, equipment);
        inventory.applyBukkitEquipmentLoadout(equipment);
    }

    private void applyConfiguredArmor(BotOptions options, Map<EquipmentSlotKind, ItemStack> equipment) {
        for (EquipmentSlotKind slot : java.util.Collections.unmodifiableList(java.util.Arrays.asList(
                EquipmentSlotKind.HEAD, EquipmentSlotKind.CHEST, EquipmentSlotKind.LEGS, EquipmentSlotKind.FEET))) {
            ItemStack configured = options.getArmor().get(slot);
            if (configured == null) {
                continue;
            }
            ItemStack equipped = configured.clone();
            BotEquipmentUtils.applyArmorEnchants(equipped, options.getBlast().getOrDefault(slot, false));
            equipment.put(slot, equipped);
        }
    }

    private void capture() {
        if (captured) {
            return;
        }
        for (int slot = 0; slot < HOTBAR_SIZE; slot++) {
            originalItems[slot] = inventory.getItem(slot).clone();
        }
        originalSelectedSlot = inventory.getCurrentSlot();
        for (EquipmentSlotKind slot : managedEquipmentSlots()) {
            originalEquipment.put(slot, inventory.getEquipment(slot).clone());
        }
        captured = true;
    }

    private void restoreOriginalEquipment() {
        if (!captured) {
            return;
        }
        inventory.applyBukkitEquipmentLoadout(originalEquipment);
    }

    private static EquipmentSlotKind[] managedEquipmentSlots() {
        EquipmentSlotKind offHand = EquipmentSlotAccess.offHand();
        if (offHand == null) {
            return new EquipmentSlotKind[] {
                EquipmentSlotKind.HEAD, EquipmentSlotKind.CHEST, EquipmentSlotKind.LEGS, EquipmentSlotKind.FEET
            };
        }
        return new EquipmentSlotKind[] {
            offHand, EquipmentSlotKind.HEAD, EquipmentSlotKind.CHEST, EquipmentSlotKind.LEGS, EquipmentSlotKind.FEET
        };
    }

    private Map<Integer, ItemStack> originalHotbar() {
        Map<Integer, ItemStack> hotbar = new HashMap<>(HOTBAR_SIZE);
        for (int slot = 0; slot < HOTBAR_SIZE; slot++) {
            hotbar.put(slot, originalItems[slot].clone());
        }
        return hotbar;
    }

    @Override
    public void close() {
        if (!captured) {
            return;
        }
        inventory.applyBukkitHotbarLoadout(originalHotbar(), originalSelectedSlot);
        restoreOriginalEquipment();
        Arrays.fill(originalItems, new ItemStack(Material.AIR));
        originalEquipment.clear();
        originalSelectedSlot = BotInventoryController.SWORD_SLOT;
        captured = false;
    }
}
