package com.monkey.mcbot.sdk.model;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

/** Persistent remote equipment setting for one bot slot. */
public record BotEquipmentSlotRequest(
        SdkBotEquipmentSlotMode mode, @Nullable String material, int amount) {
    public BotEquipmentSlotRequest {
        Objects.requireNonNull(mode, "mode");
        if (mode == SdkBotEquipmentSlotMode.ITEM) {
            if (material == null || material.isBlank()) {
                throw new IllegalArgumentException("ITEM mode requires a material");
            }
            if (amount < 1 || amount > 64) {
                throw new IllegalArgumentException("item amount must be between 1 and 64");
            }
            material = material.trim().toUpperCase(java.util.Locale.ROOT);
        } else {
            material = null;
            amount = 0;
        }
    }

    public static BotEquipmentSlotRequest defaultSlot() {
        return new BotEquipmentSlotRequest(SdkBotEquipmentSlotMode.DEFAULT, null, 0);
    }

    public static BotEquipmentSlotRequest empty() {
        return new BotEquipmentSlotRequest(SdkBotEquipmentSlotMode.EMPTY, null, 0);
    }

    public static BotEquipmentSlotRequest item(String material) {
        return item(material, 1);
    }

    public static BotEquipmentSlotRequest item(String material, int amount) {
        return new BotEquipmentSlotRequest(
                SdkBotEquipmentSlotMode.ITEM, Objects.requireNonNull(material, "material"), amount);
    }
}
