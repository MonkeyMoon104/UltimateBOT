package com.monkey.ultimatebot.access.item;

import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

final class ModernItemStackOps implements ItemStackOps {
    @Override
    public boolean isEmpty(@Nullable ItemStack stack) {
        return stack == null || stack.isEmpty();
    }

    @Override
    public ItemStack withType(ItemStack stack, Material type) {
        Objects.requireNonNull(stack, "stack");
        Objects.requireNonNull(type, "type");
        return stack.withType(type);
    }

    @Override
    public ItemStack empty() {

        return ItemStack.empty();
    }
}
