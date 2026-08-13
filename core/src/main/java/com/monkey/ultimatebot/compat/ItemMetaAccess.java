package com.monkey.ultimatebot.compat;

import java.util.List;
import java.util.Objects;
import org.bukkit.inventory.meta.ItemMeta;
import org.jspecify.annotations.Nullable;

/**
 * Item display name / lore across Paper versions.
 *
 * <p>Adventure {@code ItemMeta#displayName(Component)} / {@code lore(List)} need Adventure on the
 * server classpath. Pre-Adventure and incomplete Paper 1.16 builds only have the String APIs.
 * Prefer Strings so GUI tabs work on 1.16.5 without linking serializers at class-init.
 */
public final class ItemMetaAccess {

    private ItemMetaAccess() {}

    public static void setDisplayName(ItemMeta meta, @Nullable String name) {
        Objects.requireNonNull(meta, "meta");
        meta.setDisplayName(name);
    }

    public static void setLore(ItemMeta meta, @Nullable List<String> lore) {
        Objects.requireNonNull(meta, "meta");
        meta.setLore(lore);
    }
}
