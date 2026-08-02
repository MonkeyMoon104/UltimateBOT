package com.monkey.ultimatebot.combat.mode;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

final class ModeKit {
    private final Map<Integer, ItemStack> slots;

    private ModeKit(Map<Integer, ItemStack> slots) {
        Map<Integer, ItemStack> copy = new HashMap<>();
        slots.forEach((slot, item) -> copy.put(slot, item.copy()));
        this.slots = Collections.unmodifiableMap(copy);
    }

    static Builder builder() {
        return new Builder();
    }

    Map<Integer, ItemStack> slots() {
        Map<Integer, ItemStack> copy = new HashMap<>();
        slots.forEach((slot, item) -> copy.put(slot, item.copy()));
        return copy;
    }

    static final class Builder {
        private final Map<Integer, ItemStack> slots = new HashMap<>();

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

        ModeKit build() {
            return new ModeKit(slots);
        }
    }
}
