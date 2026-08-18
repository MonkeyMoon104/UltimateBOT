package com.monkey.ultimatebot.access.item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

interface ItemStackOps {
    boolean isEmpty(@Nullable ItemStack stack);

    ItemStack withType(ItemStack stack, Material type);

    ItemStack empty();
}
