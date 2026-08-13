package com.monkey.ultimatebot.sdk.model.request;

import com.monkey.ultimatebot.sdk.model.type.SdkBotEquipmentSlotMode;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/** Persistent remote equipment setting for one bot slot. */
public final class BotEquipmentSlotRequest {
    private final SdkBotEquipmentSlotMode mode;
    private final String material;
    private final int amount;

    public BotEquipmentSlotRequest(SdkBotEquipmentSlotMode mode, String material, int amount) {


        Objects.requireNonNull(mode, "mode");
        if (mode == SdkBotEquipmentSlotMode.ITEM) {
            if (material == null || material.trim().isEmpty()) {
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
        this.mode = mode;
        this.material = material;
        this.amount = amount;
    }

    public SdkBotEquipmentSlotMode mode() {
        return mode;
    }
    public String material() {
        return material;
    }
    public int amount() {
        return amount;
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BotEquipmentSlotRequest)) {
            return false;
        }
        BotEquipmentSlotRequest other = (BotEquipmentSlotRequest) obj;
        return java.util.Objects.equals(mode, other.mode) && java.util.Objects.equals(material, other.material) && amount == other.amount;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(mode, material, amount);
    }

    @Override
    public String toString() {
        return "BotEquipmentSlotRequest[mode=" + mode + ", material=" + material + ", amount=" + amount + "]";
    }
}
