package com.monkey.ultimatebot.compat;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Cross-version {@link ItemStack} helpers.
 *
 * <p>Dual-path like trim registries: modern Paper uses {@code withType}/{@code isEmpty}/{@code
 * empty}; older servers use legacy rebuild/AIR checks. Never hard-link modern methods from shared
 * call sites.
 */
public final class ItemStackAccess {

    private ItemStackAccess() {}

    public static boolean isEmpty(@Nullable ItemStack stack) {
        return ItemStackOpsLookup.get().isEmpty(stack);
    }

    /**
     * Returns a stack of {@code type} preserving amount/meta when possible (modern {@code
     * withType}, or legacy rebuild).
     */
    public static ItemStack withType(ItemStack stack, Material type) {
        return ItemStackOpsLookup.get().withType(stack, type);
    }

    /** Empty stack (modern {@code ItemStack.empty()}, or legacy {@code AIR}). */
    public static ItemStack empty() {
        return ItemStackOpsLookup.get().empty();
    }
}
