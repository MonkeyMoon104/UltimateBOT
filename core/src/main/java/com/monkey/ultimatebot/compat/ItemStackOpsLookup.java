package com.monkey.ultimatebot.compat;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Selects modern ({@code withType}/{@code isEmpty}/{@code empty}) or legacy ItemStack ops. Modern
 * is loaded reflectively so older Paper never links those methods.
 */
final class ItemStackOpsLookup {
    private static final ItemStackOps INSTANCE = resolve();

    private ItemStackOpsLookup() {}

    static ItemStackOps get() {
        return INSTANCE;
    }

    private static ItemStackOps resolve() {
        try {
            // Probe APIs before linking ModernItemStackOps.
            ItemStack.class.getMethod("withType", Material.class);
            ItemStack.class.getMethod("isEmpty");
            ItemStack.class.getMethod("empty");
            return Class.forName("com.monkey.ultimatebot.compat.ModernItemStackOps")
                    .asSubclass(ItemStackOps.class)
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return new LegacyItemStackOps();
        }
    }
}
