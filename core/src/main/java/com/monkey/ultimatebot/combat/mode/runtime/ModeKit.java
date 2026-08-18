package com.monkey.ultimatebot.combat.mode.runtime;

import com.monkey.ultimatebot.common.model.EquipmentSlotKind;
import com.monkey.ultimatebot.access.item.EquipmentSlotAccess;
import com.monkey.ultimatebot.access.item.ItemStackAccess;
import com.monkey.ultimatebot.utils.material.MaterialCatalog;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class ModeKit {
    private final Map<Integer, ItemStack> slots;
    private final Map<EquipmentSlotKind, ItemStack> equipment;

    private ModeKit(Map<Integer, ItemStack> slots, Map<EquipmentSlotKind, ItemStack> equipment) {
        Map<Integer, ItemStack> copy = new HashMap<>();
        slots.forEach((slot, item) -> copy.put(slot, item.clone()));
        this.slots = Collections.unmodifiableMap(copy);
        Map<EquipmentSlotKind, ItemStack> equipmentCopy = new java.util.EnumMap<>(EquipmentSlotKind.class);
        equipment.forEach((slot, item) -> equipmentCopy.put(slot, item.clone()));
        this.equipment = Collections.unmodifiableMap(equipmentCopy);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Map<Integer, ItemStack> slots() {
        Map<Integer, ItemStack> copy = new HashMap<>();
        slots.forEach((slot, item) -> copy.put(slot, item.clone()));
        return copy;
    }

    public Map<EquipmentSlotKind, ItemStack> equipment() {
        Map<EquipmentSlotKind, ItemStack> copy = new java.util.EnumMap<>(EquipmentSlotKind.class);
        equipment.forEach((slot, item) -> copy.put(slot, item.clone()));
        return copy;
    }

    public static final class Builder {
        private final Map<Integer, ItemStack> slots = new HashMap<>();
        private final Map<EquipmentSlotKind, ItemStack> equipment = new java.util.EnumMap<>(EquipmentSlotKind.class);

        public Builder slot(int slot, Material item) {
            return slot(slot, item, 1);
        }

        public Builder slot(int slot, Material item, int count) {
            if (slot < 0 || slot > 8) {
                throw new IllegalArgumentException("Hotbar slot must be between 0 and 8: " + slot);
            }
            if (count < 1 || count > 99) {
                throw new IllegalArgumentException("Item count must be between 1 and 99: " + count);
            }
            slots.put(slot, new ItemStack(Objects.requireNonNull(item, "item"), count));
            return this;
        }

        public Builder slot(int slot, String materialName, int count) {
            return slot(slot, MaterialCatalog.require(Objects.requireNonNull(materialName, "materialName")), count);
        }

        public Builder slot(int slot, String materialName, Material fallback, int count) {
            return slot(slot, MaterialCatalog.optional(materialName, fallback), count);
        }

        public Builder equipment(EquipmentSlotKind slot, String materialName, Material fallback) {
            return equipment(slot, MaterialCatalog.optional(materialName, fallback));
        }

        public Builder offHand(Material item) {
            EquipmentSlotKind offHand = EquipmentSlotAccess.offHand();
            if (offHand == null) {
                return this;
            }
            return equipment(offHand, item);
        }

        public Builder offHand(String materialName, Material fallback) {
            EquipmentSlotKind offHand = EquipmentSlotAccess.offHand();
            if (offHand == null) {
                return this;
            }
            return equipment(offHand, materialName, fallback);
        }

        public Builder slot(int slot, ItemStack item) {
            if (slot < 0 || slot > 8) {
                throw new IllegalArgumentException("Hotbar slot must be between 0 and 8: " + slot);
            }
            ItemStack checkedItem = Objects.requireNonNull(item, "item");
            if (ItemStackAccess.isEmpty(checkedItem)) {
                throw new IllegalArgumentException("Mode kit item cannot be empty");
            }
            slots.put(slot, checkedItem.clone());
            return this;
        }

        public Builder equipment(EquipmentSlotKind slot, Material item) {
            EquipmentSlotKind checkedSlot = Objects.requireNonNull(slot, "slot");
            if (checkedSlot == EquipmentSlotKind.HAND) {
                throw new IllegalArgumentException("Unsupported mode equipment slot: " + checkedSlot);
            }
            equipment.put(checkedSlot, new ItemStack(Objects.requireNonNull(item, "item")));
            return this;
        }

        public Builder equipment(EquipmentSlotKind slot, ItemStack item) {
            EquipmentSlotKind checkedSlot = Objects.requireNonNull(slot, "slot");
            if (checkedSlot == EquipmentSlotKind.HAND) {
                throw new IllegalArgumentException("Unsupported mode equipment slot: " + checkedSlot);
            }
            ItemStack checkedItem = Objects.requireNonNull(item, "item");
            if (ItemStackAccess.isEmpty(checkedItem)) {
                throw new IllegalArgumentException("Mode equipment item cannot be empty");
            }
            equipment.put(checkedSlot, checkedItem.clone());
            return this;
        }

        public ModeKit build() {
            return new ModeKit(slots, equipment);
        }
    }
}
