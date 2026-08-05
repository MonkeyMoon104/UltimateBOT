package com.monkey.ultimatebot.extension.runtime;

import com.monkey.ultimatebot.combat.mode.runtime.ModeKit;
import com.monkey.ultimatebot.utils.equipment.EquipmentConverter;
import org.bukkit.craftbukkit.inventory.CraftItemStack;

public final class ModeKitAdapter {
    private ModeKitAdapter() {}

    public static ModeKit toInternal(com.monkey.ultimatebot.api.extension.combat.ModeKit kit) {
        ModeKit.Builder builder = ModeKit.builder();
        kit.inventory().forEach((slot, item) -> {
            net.minecraft.world.item.ItemStack converted = CraftItemStack.asNMSCopy(item);
            if (!converted.isEmpty()) {
                builder.slot(slot, converted);
            }
        });
        kit.equipment().forEach((slot, item) -> {
            net.minecraft.world.entity.EquipmentSlot convertedSlot = EquipmentConverter.toNMSSlot(slot);
            net.minecraft.world.item.ItemStack convertedItem = CraftItemStack.asNMSCopy(item);
            if (convertedSlot != null && !convertedItem.isEmpty()) {
                builder.equipment(convertedSlot, convertedItem);
            }
        });
        return builder.build();
    }
}
