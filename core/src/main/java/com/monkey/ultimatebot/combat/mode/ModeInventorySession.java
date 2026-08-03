package com.monkey.ultimatebot.combat.mode;

import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.utils.equipment.BotEquipmentUtils;
import com.monkey.ultimatebot.utils.equipment.EquipmentConverter;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.inventory.CraftItemStack;

final class ModeInventorySession implements AutoCloseable {
    private static final int HOTBAR_SIZE = 9;

    private final BotInventoryController inventory;
    private final ItemStack[] originalItems = new ItemStack[HOTBAR_SIZE];
    private final Map<EquipmentSlot, ItemStack> originalEquipment = new EnumMap<>(EquipmentSlot.class);
    private int originalSelectedSlot;
    private boolean captured;

    ModeInventorySession(BotInventoryController inventory) {
        this.inventory = Objects.requireNonNull(inventory, "inventory");
    }

    void apply(ModeKit kit, BotOptions options) {
        Objects.requireNonNull(kit, "kit");
        Objects.requireNonNull(options, "options");
        capture();
        Map<Integer, ItemStack> hotbar = originalHotbar();
        for (Map.Entry<Integer, ItemStack> entry : kit.slots().entrySet()) {
            hotbar.put(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<Integer, org.bukkit.inventory.ItemStack> entry :
                options.getEquipmentContents().entrySet()) {
            hotbar.put(entry.getKey(), CraftItemStack.asNMSCopy(entry.getValue()));
        }
        inventory.applyHotbarLoadout(hotbar, BotInventoryController.SWORD_SLOT);

        Map<EquipmentSlot, ItemStack> equipment = new EnumMap<>(originalEquipment);
        for (Map.Entry<EquipmentSlot, ItemStack> entry : kit.equipment().entrySet()) {
            equipment.put(entry.getKey(), entry.getValue());
        }
        applyConfiguredArmor(options, equipment);
        inventory.applyEquipmentLoadout(equipment);
    }

    private void applyConfiguredArmor(BotOptions options, Map<EquipmentSlot, ItemStack> equipment) {
        for (org.bukkit.inventory.EquipmentSlot bukkitSlot : EquipmentConverter.getArmorSlots()) {
            org.bukkit.inventory.ItemStack configured = options.getArmor().get(bukkitSlot);
            EquipmentSlot nmsSlot = EquipmentConverter.toNMSSlot(bukkitSlot);
            if (configured == null || nmsSlot == null) {
                continue;
            }
            org.bukkit.inventory.ItemStack equipped = configured.clone();
            BotEquipmentUtils.applyArmorEnchants(equipped, options.getBlast().getOrDefault(bukkitSlot, false));
            equipment.put(nmsSlot, CraftItemStack.asNMSCopy(equipped));
        }
    }

    private void capture() {
        if (captured) {
            return;
        }
        for (int slot = 0; slot < HOTBAR_SIZE; slot++) {
            originalItems[slot] = inventory.getItem(slot).copy();
        }
        originalSelectedSlot = inventory.getCurrentSlot();
        for (EquipmentSlot slot : managedEquipmentSlots()) {
            originalEquipment.put(slot, inventory.getEquipment(slot).copy());
        }
        captured = true;
    }

    private void restoreOriginalEquipment() {
        if (!captured) {
            return;
        }
        inventory.applyEquipmentLoadout(originalEquipment);
    }

    private static EquipmentSlot[] managedEquipmentSlots() {
        return new EquipmentSlot[] {
            EquipmentSlot.OFFHAND, EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
        };
    }

    private Map<Integer, ItemStack> originalHotbar() {
        Map<Integer, ItemStack> hotbar = new HashMap<>(HOTBAR_SIZE);
        for (int slot = 0; slot < HOTBAR_SIZE; slot++) {
            hotbar.put(slot, originalItems[slot].copy());
        }
        return hotbar;
    }

    @Override
    public void close() {
        if (!captured) {
            return;
        }
        inventory.applyHotbarLoadout(originalHotbar(), originalSelectedSlot);
        restoreOriginalEquipment();
        Arrays.fill(originalItems, ItemStack.EMPTY);
        originalEquipment.clear();
        originalSelectedSlot = BotInventoryController.SWORD_SLOT;
        captured = false;
    }
}
