package com.monkey.ultimatebot.access.item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

public final class ItemStackAccess {

    private ItemStackAccess() {}

    public static boolean isEmpty(@Nullable ItemStack stack) {
        return ItemStackOpsLookup.get().isEmpty(stack);
    }

    public static ItemStack withType(ItemStack stack, Material type) {
        return ItemStackOpsLookup.get().withType(stack, type);
    }

    public static ItemStack empty() {
        return ItemStackOpsLookup.get().empty();
    }
}
