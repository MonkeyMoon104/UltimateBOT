package com.monkey.ultimatebot.compat;

import org.bukkit.inventory.EquipmentSlot;
import org.jspecify.annotations.Nullable;

/**
 * Bukkit {@link EquipmentSlot} members that are missing on 1.8.
 *
 * <p>{@code OFF_HAND} exists from 1.9 (dual wield). A direct {@code EquipmentSlot.OFF_HAND}
 * getstatic on Spigot 1.8 throws {@link NoSuchFieldError}. Resolve the constant only after the
 * version check so 1.9+ call sites are unchanged.
 */
public final class EquipmentSlotAccess {

    private EquipmentSlotAccess() {}

    public static boolean hasOffHand() {
        return MinecraftVersionAccess.isAtLeast(1, 9);
    }

    public static @Nullable EquipmentSlot offHand() {
        if (!hasOffHand()) {
            return null;
        }
        return EquipmentSlot.OFF_HAND;
    }
}
