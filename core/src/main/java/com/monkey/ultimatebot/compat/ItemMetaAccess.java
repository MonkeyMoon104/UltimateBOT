package com.monkey.ultimatebot.compat;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import org.bukkit.inventory.meta.ItemMeta;
import org.jspecify.annotations.Nullable;

/**
 * Item display name / lore / unbreakable across Bukkit revisions.
 *
 * <p>Adventure {@code ItemMeta#displayName(Component)} / {@code lore(List)} need Adventure on the
 * server classpath. Pre-Adventure and incomplete Paper 1.16 builds only have the String APIs.
 * Prefer Strings so GUI tabs work on 1.16.5 without linking serializers at class-init.
 *
 * <p>{@code ItemMeta#setUnbreakable} exists from 1.11. 1.8–1.10 keep it on {@code ItemMeta.spigot()}.
 * Hard calls crash {@code /bot} on 1.10.2 with {@code NoSuchMethodError}.
 */
public final class ItemMetaAccess {

    private static final @Nullable Method SET_UNBREAKABLE =
            method(ItemMeta.class, "setUnbreakable", boolean.class);
    private static final @Nullable Method IS_UNBREAKABLE = method(ItemMeta.class, "isUnbreakable");
    private static final @Nullable Method SPIGOT = method(ItemMeta.class, "spigot");
    private static final @Nullable Class<?> SPIGOT_TYPE =
            classOrNull("org.bukkit.inventory.meta.ItemMeta$Spigot");
    private static final @Nullable Method SPIGOT_SET_UNBREAKABLE =
            method(SPIGOT_TYPE, "setUnbreakable", boolean.class);
    private static final @Nullable Method SPIGOT_IS_UNBREAKABLE = method(SPIGOT_TYPE, "isUnbreakable");

    private ItemMetaAccess() {}

    public static void setDisplayName(ItemMeta meta, @Nullable String name) {
        Objects.requireNonNull(meta, "meta");
        meta.setDisplayName(name);
    }

    public static void setLore(ItemMeta meta, @Nullable List<String> lore) {
        Objects.requireNonNull(meta, "meta");
        meta.setLore(lore);
    }

    public static void setUnbreakable(ItemMeta meta, boolean unbreakable) {
        Objects.requireNonNull(meta, "meta");
        if (SET_UNBREAKABLE != null) {
            try {
                SET_UNBREAKABLE.invoke(meta, Boolean.valueOf(unbreakable));
                return;
            } catch (ReflectiveOperationException | LinkageError ignored) {
                // fall through to Spigot nested API
            }
        }
        Object spigot = spigot(meta);
        if (spigot == null || SPIGOT_SET_UNBREAKABLE == null) {
            return;
        }
        try {
            SPIGOT_SET_UNBREAKABLE.invoke(spigot, Boolean.valueOf(unbreakable));
        } catch (ReflectiveOperationException | LinkageError ignored) {
            // leave item breakable rather than crash /bot
        }
    }

    public static boolean isUnbreakable(ItemMeta meta) {
        Objects.requireNonNull(meta, "meta");
        if (IS_UNBREAKABLE != null) {
            try {
                Object result = IS_UNBREAKABLE.invoke(meta);
                return result instanceof Boolean && ((Boolean) result).booleanValue();
            } catch (ReflectiveOperationException | LinkageError ignored) {
                // fall through
            }
        }
        Object spigot = spigot(meta);
        if (spigot == null || SPIGOT_IS_UNBREAKABLE == null) {
            return false;
        }
        try {
            Object result = SPIGOT_IS_UNBREAKABLE.invoke(spigot);
            return result instanceof Boolean && ((Boolean) result).booleanValue();
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return false;
        }
    }

    private static @Nullable Object spigot(ItemMeta meta) {
        if (SPIGOT == null) {
            return null;
        }
        try {
            return SPIGOT.invoke(meta);
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return null;
        }
    }

    private static @Nullable Method method(@Nullable Class<?> type, String name, Class<?>... params) {
        if (type == null) {
            return null;
        }
        try {
            return type.getMethod(name, params);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    private static @Nullable Class<?> classOrNull(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException | LinkageError ignored) {
            return null;
        }
    }
}
