package com.monkey.ultimatebot.combat.mode;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

final class ModeKit {
    private final Map<Integer, ItemStack> slots;
    private final Map<EquipmentSlot, ItemStack> equipment;

    private ModeKit(Map<Integer, ItemStack> slots, Map<EquipmentSlot, ItemStack> equipment) {
        Map<Integer, ItemStack> copy = new HashMap<>();
        slots.forEach((slot, item) -> copy.put(slot, item.copy()));
        this.slots = Collections.unmodifiableMap(copy);
        Map<EquipmentSlot, ItemStack> equipmentCopy = new java.util.EnumMap<>(EquipmentSlot.class);
        equipment.forEach((slot, item) -> equipmentCopy.put(slot, item.copy()));
        this.equipment = Collections.unmodifiableMap(equipmentCopy);
    }

    static Builder builder() {
        return new Builder();
    }

    Map<Integer, ItemStack> slots() {
        Map<Integer, ItemStack> copy = new HashMap<>();
        slots.forEach((slot, item) -> copy.put(slot, item.copy()));
        return copy;
    }

    Map<EquipmentSlot, ItemStack> equipment() {
        Map<EquipmentSlot, ItemStack> copy = new java.util.EnumMap<>(EquipmentSlot.class);
        equipment.forEach((slot, item) -> copy.put(slot, item.copy()));
        return copy;
    }

    static final class Builder {
        private final Map<Integer, ItemStack> slots = new HashMap<>();
        private final Map<EquipmentSlot, ItemStack> equipment = new java.util.EnumMap<>(EquipmentSlot.class);

        Builder slot(int slot, Item item) {
            return slot(slot, item, 1);
        }

        Builder slot(int slot, Item item, int count) {
            if (slot < 0 || slot > 8) {
                throw new IllegalArgumentException("Hotbar slot must be between 0 and 8: " + slot);
            }
            if (count < 1 || count > 99) {
                throw new IllegalArgumentException("Item count must be between 1 and 99: " + count);
            }
            slots.put(slot, new ItemStack(Objects.requireNonNull(item, "item"), count));
            return this;
        }

        Builder slot(int slot, ItemStack item) {
            if (slot < 0 || slot > 8) {
                throw new IllegalArgumentException("Hotbar slot must be between 0 and 8: " + slot);
            }
            ItemStack checkedItem = Objects.requireNonNull(item, "item");
            if (checkedItem.isEmpty()) {
                throw new IllegalArgumentException("Mode kit item cannot be empty");
            }
            slots.put(slot, checkedItem.copy());
            return this;
        }

        Builder equipment(EquipmentSlot slot, Item item) {
            EquipmentSlot checkedSlot = Objects.requireNonNull(slot, "slot");
            if (checkedSlot == EquipmentSlot.MAINHAND || checkedSlot == EquipmentSlot.BODY) {
                throw new IllegalArgumentException("Unsupported mode equipment slot: " + checkedSlot);
            }
            equipment.put(checkedSlot, new ItemStack(Objects.requireNonNull(item, "item")));
            return this;
        }

        ModeKit build() {
            return new ModeKit(slots, equipment);
        }
    }
}
