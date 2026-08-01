package com.monkey.mcbot.api.model;

import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

/** Immutable persistent setting for one bot equipment slot. */
public final class BotEquipmentSlotSetting {
    private static final BotEquipmentSlotSetting DEFAULT =
            new BotEquipmentSlotSetting(BotEquipmentSlotMode.DEFAULT, null);
    private static final BotEquipmentSlotSetting EMPTY = new BotEquipmentSlotSetting(BotEquipmentSlotMode.EMPTY, null);

    private final BotEquipmentSlotMode mode;
    private final @Nullable ItemStack item;

    private BotEquipmentSlotSetting(BotEquipmentSlotMode mode, @Nullable ItemStack item) {
        this.mode = Objects.requireNonNull(mode, "mode");
        this.item = item == null ? null : item.clone();
    }

    public static BotEquipmentSlotSetting defaultSlot() {
        return DEFAULT;
    }

    public static BotEquipmentSlotSetting empty() {
        return EMPTY;
    }

    public static BotEquipmentSlotSetting item(ItemStack item) {
        ItemStack required = Objects.requireNonNull(item, "item");
        if (required.getType() == Material.AIR || required.getAmount() <= 0) {
            throw new IllegalArgumentException("item setting requires a non-empty item");
        }
        return new BotEquipmentSlotSetting(BotEquipmentSlotMode.ITEM, required);
    }

    public BotEquipmentSlotMode mode() {
        return mode;
    }

    public @Nullable ItemStack item() {
        return item == null ? null : item.clone();
    }
}
