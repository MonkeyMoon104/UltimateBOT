package com.monkey.mcbot.utils.equipment;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EquipmentConverter {

    public static EquipmentSlot toNMSSlot(org.bukkit.inventory.EquipmentSlot bukkitSlot) {
        return switch (bukkitSlot) {
            case HEAD -> EquipmentSlot.HEAD;
            case CHEST -> EquipmentSlot.CHEST;
            case LEGS -> EquipmentSlot.LEGS;
            case FEET -> EquipmentSlot.FEET;
            case HAND -> EquipmentSlot.MAINHAND;
            case OFF_HAND -> EquipmentSlot.OFFHAND;
            case BODY -> null;
        };
    }

    public static Material toMaterial(ItemStack nmsItem) {
        if (nmsItem == null || nmsItem.isEmpty()) {
            return Material.AIR;
        }
        org.bukkit.inventory.ItemStack bukkitItem = CraftItemStack.asBukkitCopy(nmsItem);
        return bukkitItem.getType();
    }

    public static org.bukkit.inventory.EquipmentSlot[] getArmorSlots() {
        return new org.bukkit.inventory.EquipmentSlot[]{
                org.bukkit.inventory.EquipmentSlot.HEAD,
                org.bukkit.inventory.EquipmentSlot.CHEST,
                org.bukkit.inventory.EquipmentSlot.LEGS,
                org.bukkit.inventory.EquipmentSlot.FEET
        };
    }
}