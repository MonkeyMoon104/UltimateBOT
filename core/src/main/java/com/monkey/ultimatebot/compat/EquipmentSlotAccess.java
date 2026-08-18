package com.monkey.ultimatebot.compat;

import com.monkey.ultimatebot.common.model.EquipmentSlotKind;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * Bukkit {@code org.bukkit.inventory.EquipmentSlot} is missing entirely on 1.7.10 and lacks
 * {@code OFF_HAND} on 1.8.
 *
 * <p>Core and NMS bridges use {@link EquipmentSlotKind} so 1.7 never links the Bukkit enum. This
 * helper only reflects the Bukkit type when a call site still has a live Bukkit slot object (1.8+).
 */
public final class EquipmentSlotAccess {

    private static final boolean BUKKIT_SLOT_CLASS = hasBukkitSlotClass();

    private EquipmentSlotAccess() {}

    public static boolean hasBukkitClass() {
        return BUKKIT_SLOT_CLASS;
    }

    public static boolean hasOffHand() {
        return MinecraftVersionAccess.isAtLeast(1, 9);
    }

    public static @Nullable EquipmentSlotKind offHand() {
        return hasOffHand() ? EquipmentSlotKind.OFF_HAND : null;
    }

    public static EquipmentSlotKind[] armorSlots() {
        return new EquipmentSlotKind[] {
            EquipmentSlotKind.HEAD,
            EquipmentSlotKind.CHEST,
            EquipmentSlotKind.LEGS,
            EquipmentSlotKind.FEET
        };
    }

    /**
     * Maps a Bukkit {@code EquipmentSlot} (or any enum whose {@code name()} matches) to
     * {@link EquipmentSlotKind}. Returns {@code null} when the class/constant is absent.
     */
    public static @Nullable EquipmentSlotKind fromBukkit(@Nullable Object bukkitSlot) {
        if (bukkitSlot == null) {
            return null;
        }
        try {
            return EquipmentSlotKind.valueOf(((Enum<?>) bukkitSlot).name());
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    public static <V> Map<EquipmentSlotKind, V> mapKeys(@Nullable Map<?, V> source) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyMap();
        }
        EnumMap<EquipmentSlotKind, V> mapped = new EnumMap<>(EquipmentSlotKind.class);
        for (Map.Entry<?, V> entry : source.entrySet()) {
            EquipmentSlotKind kind = fromBukkit(entry.getKey());
            if (kind != null) {
                mapped.put(kind, entry.getValue());
            }
        }
        return mapped;
    }

    private static boolean hasBukkitSlotClass() {
        try {
            Class.forName("org.bukkit.inventory.EquipmentSlot");
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }
}
