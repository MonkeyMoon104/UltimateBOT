package com.monkey.ultimatebot.compat;

import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jspecify.annotations.Nullable;

/**
 * Pre-{@code withType}/{@code isEmpty}/{@code empty} Paper/Bukkit: rebuild stacks and use AIR
 * without linking modern methods.
 */
final class LegacyItemStackOps implements ItemStackOps {
    @Override
    public boolean isEmpty(@Nullable ItemStack stack) {
        if (stack == null) {
            return true;
        }
        Material type = stack.getType();
        return type == Material.AIR || stack.getAmount() <= 0;
    }

    @Override
    public ItemStack withType(ItemStack stack, Material type) {
        Objects.requireNonNull(stack, "stack");
        Objects.requireNonNull(type, "type");
        if (stack.getType() == type) {
            return stack.clone();
        }
        ItemStack rebuilt = new ItemStack(type, Math.max(1, stack.getAmount()));
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            rebuilt.setItemMeta(meta.clone());
        }
        return rebuilt;
    }

    @Override
    public ItemStack empty() {
        return new ItemStack(Material.AIR);
    }
}
