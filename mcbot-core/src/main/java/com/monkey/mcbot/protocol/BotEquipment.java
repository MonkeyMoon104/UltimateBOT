package com.monkey.mcbot.protocol;

import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public record BotEquipment(EquipmentSlot slot, ItemStack item) {

    public BotEquipment {
        Objects.requireNonNull(slot, "slot");
        Objects.requireNonNull(item, "item");
    }
}
