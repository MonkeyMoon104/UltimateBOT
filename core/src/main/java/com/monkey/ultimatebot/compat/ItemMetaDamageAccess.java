package com.monkey.ultimatebot.compat;

import java.lang.reflect.Method;
import java.util.Objects;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jspecify.annotations.Nullable;

/**
 * Clears item damage without linking {@code org.bukkit.inventory.meta.Damageable}.
 *
 * <p>{@code Damageable} exists from 1.13+. Pre-flattening servers use {@link ItemStack#setDurability(short)}.
 */
public final class ItemMetaDamageAccess {

    private static final @Nullable Class<?> DAMAGEABLE_TYPE =
            classOrNull("org.bukkit.inventory.meta.Damageable");
    private static final @Nullable Method HAS_DAMAGE = method(DAMAGEABLE_TYPE, "hasDamage");
    private static final @Nullable Method SET_DAMAGE = method(DAMAGEABLE_TYPE, "setDamage", int.class);

    private ItemMetaDamageAccess() {}

    /**
     * Clears damage on armor/tools. Mutates {@code meta} (modern) or {@code stack} (legacy durability).
     *
     * @return {@code true} when something changed and the caller should re-apply meta / re-equip
     */
    public static boolean clearDamage(ItemStack stack, ItemMeta meta) {
        Objects.requireNonNull(stack, "stack");
        Objects.requireNonNull(meta, "meta");
        if (DAMAGEABLE_TYPE != null && HAS_DAMAGE != null && SET_DAMAGE != null && DAMAGEABLE_TYPE.isInstance(meta)) {
            try {
                Object hasDamage = HAS_DAMAGE.invoke(meta);
                if (hasDamage instanceof Boolean && ((Boolean) hasDamage).booleanValue()) {
                    SET_DAMAGE.invoke(meta, Integer.valueOf(0));
                    return true;
                }
            } catch (ReflectiveOperationException | LinkageError ignored) {
                // fall through to legacy durability
            }
            return false;
        }
        try {
            if (stack.getDurability() != 0) {
                stack.setDurability((short) 0);
                return true;
            }
        } catch (NoSuchMethodError ignored) {
            // very modern API without durability
        }
        return false;
    }

    private static @Nullable Class<?> classOrNull(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException | LinkageError ignored) {
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
}
