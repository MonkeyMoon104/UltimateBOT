package com.monkey.ultimatebot.access.item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

final class ItemStackOpsLookup {
    private static final ItemStackOps INSTANCE = resolve();

    private ItemStackOpsLookup() {}

    static ItemStackOps get() {
        return INSTANCE;
    }

    private static ItemStackOps resolve() {
        try {

            ItemStack.class.getMethod("withType", Material.class);
            ItemStack.class.getMethod("isEmpty");
            ItemStack.class.getMethod("empty");
            return Class.forName("com.monkey.ultimatebot.access.item.ModernItemStackOps")
                    .asSubclass(ItemStackOps.class)
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return new LegacyItemStackOps();
        }
    }
}
