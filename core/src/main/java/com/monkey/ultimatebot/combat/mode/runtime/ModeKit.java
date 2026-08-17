package com.monkey.ultimatebot.combat.mode.runtime;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import com.monkey.ultimatebot.compat.EquipmentSlotAccess;
import com.monkey.ultimatebot.compat.ItemStackAccess;
import com.monkey.ultimatebot.utils.material.MaterialCatalog;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public final class ModeKit {
    private final Map<Integer, ItemStack> slots;
    private final Map<EquipmentSlot, ItemStack> equipment;

    private ModeKit(Map<Integer, ItemStack> slots, Map<EquipmentSlot, ItemStack> equipment) {
        Map<Integer, ItemStack> copy = new HashMap<>();
        slots.forEach((slot, item) -> copy.put(slot, item.clone()));
        this.slots = Collections.unmodifiableMap(copy);
        Map<EquipmentSlot, ItemStack> equipmentCopy = new java.util.EnumMap<>(EquipmentSlot.class);
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

    public Map<EquipmentSlot, ItemStack> equipment() {
        Map<EquipmentSlot, ItemStack> copy = new java.util.EnumMap<>(EquipmentSlot.class);
        equipment.forEach((slot, item) -> copy.put(slot, item.clone()));
        return copy;
    }

    public static final class Builder {
        private final Map<Integer, ItemStack> slots = new HashMap<>();
        private final Map<EquipmentSlot, ItemStack> equipment = new java.util.EnumMap<>(EquipmentSlot.class);

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

        /**
         * Resolves materials by name so kits do not touch version-specific {@link Material} enum
         * constants at class-init time (avoids {@code NoSuchFieldError} on older servers).
         */
        public Builder slot(int slot, String materialName, int count) {
            return slot(slot, MaterialCatalog.require(Objects.requireNonNull(materialName, "materialName")), count);
        }

        public Builder slot(int slot, String materialName, Material fallback, int count) {
            return slot(slot, MaterialCatalog.optional(materialName, fallback), count);
        }

        public Builder equipment(EquipmentSlot slot, String materialName, Material fallback) {
            return equipment(slot, MaterialCatalog.optional(materialName, fallback));
        }

        public Builder offHand(Material item) {
            EquipmentSlot offHand = EquipmentSlotAccess.offHand();
            if (offHand == null) {
                return this;
            }
            return equipment(offHand, item);
        }

        public Builder offHand(String materialName, Material fallback) {
            EquipmentSlot offHand = EquipmentSlotAccess.offHand();
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

        public Builder equipment(EquipmentSlot slot, Material item) {
            EquipmentSlot checkedSlot = Objects.requireNonNull(slot, "slot");
            if (checkedSlot == EquipmentSlot.HAND) {
                throw new IllegalArgumentException("Unsupported mode equipment slot: " + checkedSlot);
            }
            equipment.put(checkedSlot, new ItemStack(Objects.requireNonNull(item, "item")));
            return this;
        }

        public Builder equipment(EquipmentSlot slot, ItemStack item) {
            EquipmentSlot checkedSlot = Objects.requireNonNull(slot, "slot");
            if (checkedSlot == EquipmentSlot.HAND) {
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
