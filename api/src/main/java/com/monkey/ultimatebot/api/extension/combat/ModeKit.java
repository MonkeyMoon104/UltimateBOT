package com.monkey.ultimatebot.api.extension.combat;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/** Immutable inventory and armor template applied atomically when a custom mode starts. */
public final class ModeKit {
    private final Map<Integer, ItemStack> inventory;
    private final Map<EquipmentSlot, ItemStack> equipment;

    private ModeKit(Builder builder) {
        this.inventory = immutableClone(builder.inventory);
        this.equipment = immutableClone(builder.equipment);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static ModeKit empty() {
        return builder().build();
    }

    public Map<Integer, ItemStack> inventory() {
        return cloneMap(inventory);
    }

    public Map<EquipmentSlot, ItemStack> equipment() {
        return cloneMap(equipment);
    }

    private static <K> Map<K, ItemStack> immutableClone(Map<K, ItemStack> source) {
        return Collections.unmodifiableMap(cloneMap(source));
    }

    private static <K> Map<K, ItemStack> cloneMap(Map<K, ItemStack> source) {
        Map<K, ItemStack> copy = new LinkedHashMap<>();
        source.forEach((key, item) -> copy.put(key, item.clone()));
        return copy;
    }

    public static final class Builder {
        private final Map<Integer, ItemStack> inventory = new LinkedHashMap<>();
        private final Map<EquipmentSlot, ItemStack> equipment = new LinkedHashMap<>();

        private Builder() {}

        public Builder inventoryItem(int slot, ItemStack item) {
            if (slot < 0 || slot > 8) {
                throw new IllegalArgumentException("slot must be between 0 and 8");
            }
            inventory.put(slot, Objects.requireNonNull(item, "item").clone());
            return this;
        }

        public Builder equipment(EquipmentSlot slot, ItemStack item) {
            equipment.put(
                    Objects.requireNonNull(slot, "slot"),
                    Objects.requireNonNull(item, "item").clone());
            return this;
        }

        public ModeKit build() {
            return new ModeKit(this);
        }
    }

    @Override
    public String toString() {
        return "ModeKit[inventorySlots=" + inventory.size() + ", equipmentSlots=" + equipment.size() + "]";
    }
}
