package com.monkey.ultimatebot.compat;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

/** Versioned ItemStack operations (Paper modern vs pre-withType / pre-isEmpty / pre-empty). */
interface ItemStackOps {
    boolean isEmpty(@Nullable ItemStack stack);

    ItemStack withType(ItemStack stack, Material type);

    ItemStack empty();
}
