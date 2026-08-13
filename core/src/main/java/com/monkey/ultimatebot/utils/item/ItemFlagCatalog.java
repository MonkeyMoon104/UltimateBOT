package com.monkey.ultimatebot.utils.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.inventory.ItemFlag;

/** Resolves item flags safely across Bukkit API naming/version differences. */
public final class ItemFlagCatalog {

    private ItemFlagCatalog() {}

    public static List<ItemFlag> resolve(String... names) {
        if (names == null || names.length == 0) {
            return Collections.emptyList();
        }
        List<ItemFlag> resolved = new ArrayList<ItemFlag>(names.length);
        for (String name : names) {
            if (name == null || name.trim().isEmpty()) {
                continue;
            }
            try {
                resolved.add(ItemFlag.valueOf(name.trim()));
            } catch (IllegalArgumentException ignored) {
                // Flag not present in this Bukkit version.
            }
        }
        return Collections.unmodifiableList(resolved);
    }
}
