package com.monkey.ultimatebot.utils.equipment;

import com.monkey.ultimatebot.bot.BotRegistry;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.nms.NMSBridgeManager;
import java.util.*;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;
import org.jspecify.annotations.Nullable;

public class BotEquipmentUtils {
    public static void applyEquipment(
            ITrainingBot bot,
            Map<org.bukkit.inventory.EquipmentSlot, org.bukkit.inventory.ItemStack> armorMap,
            Map<org.bukkit.inventory.EquipmentSlot, Boolean> blastProtectionMap) {
        for (var entry : armorMap.entrySet()) {
            org.bukkit.inventory.EquipmentSlot slot = entry.getKey();
            if (slot == null || entry.getValue() == null) {
                continue;
            }
            org.bukkit.inventory.ItemStack bukkitItem = entry.getValue().clone();
            boolean hasBlastProtection = blastProtectionMap.getOrDefault(slot, false);
            applyArmorEnchants(bukkitItem, hasBlastProtection);
            bot.setItem(slot, bukkitItem);
        }
    }

    public static void applyArmorEnchants(org.bukkit.inventory.ItemStack item, boolean hasBlastProtection) {
        if (item == null || item.getType() == Material.AIR) return;

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.removeEnchant(Enchantment.BLAST_PROTECTION);
            meta.removeEnchant(Enchantment.PROTECTION);

            if (hasBlastProtection) {
                meta.addEnchant(Enchantment.BLAST_PROTECTION, 4, false);
            } else {
                meta.addEnchant(Enchantment.PROTECTION, 4, false);
            }

            item.setItemMeta(meta);
        }
    }

    public static void broadcastEquipment(
            ITrainingBot bot,
            Map<org.bukkit.inventory.EquipmentSlot, org.bukkit.inventory.ItemStack> armorMap,
            Map<org.bukkit.inventory.EquipmentSlot, Boolean> blastProtectionMap) {
        Map<org.bukkit.inventory.EquipmentSlot, org.bukkit.inventory.ItemStack> equipment = new EnumMap<>(
                org.bukkit.inventory.EquipmentSlot.class);

        for (var entry : armorMap.entrySet()) {
            org.bukkit.inventory.EquipmentSlot slot = entry.getKey();
            if (slot == null || entry.getValue() == null) {
                continue;
            }
            org.bukkit.inventory.ItemStack bukkitItem = entry.getValue().clone();
            boolean hasBlastProtection = blastProtectionMap.getOrDefault(slot, false);
            applyArmorEnchants(bukkitItem, hasBlastProtection);
            equipment.put(slot, bukkitItem);
        }

        if (!equipment.isEmpty()) {
            NMSBridgeManager.get().broadcastEquipment(bot, equipment);
        }
    }

    public static void sendCurrentEquipmentToViewer(ITrainingBot bot, org.bukkit.entity.Player viewer) {
        Map<org.bukkit.inventory.EquipmentSlot, org.bukkit.inventory.ItemStack> equipment = new EnumMap<>(
                org.bukkit.inventory.EquipmentSlot.class);

        equipment.put(org.bukkit.inventory.EquipmentSlot.HAND, bot.getItem(org.bukkit.inventory.EquipmentSlot.HAND));
        equipment.put(
                org.bukkit.inventory.EquipmentSlot.OFF_HAND, bot.getItem(org.bukkit.inventory.EquipmentSlot.OFF_HAND));
        for (org.bukkit.inventory.EquipmentSlot slot : EquipmentConverter.getArmorSlots()) {
            equipment.put(slot, bot.getItem(slot));
        }

        NMSBridgeManager.get().sendEquipment(viewer, bot, equipment);
    }

    public static @Nullable Map<org.bukkit.inventory.EquipmentSlot, org.bukkit.inventory.ItemStack> getBotArmor(
            UUID playerUUID, BotRegistry botRegistry) {
        UUID botUUID = botRegistry.getBotUUID(playerUUID);
        if (botUUID == null) return null;

        ITrainingBot bot = botRegistry.getBot(playerUUID);
        if (bot == null || !bot.getUniqueId().equals(botUUID)) return null;

        Map<org.bukkit.inventory.EquipmentSlot, org.bukkit.inventory.ItemStack> armor =
                new EnumMap<>(org.bukkit.inventory.EquipmentSlot.class);

        for (org.bukkit.inventory.EquipmentSlot slot : EquipmentConverter.getArmorSlots()) {
            armor.put(slot, bot.getItem(slot));
        }

        return armor;
    }

    public static void updateBotArmor(
            UUID ownerUUID,
            Map<org.bukkit.inventory.EquipmentSlot, org.bukkit.inventory.ItemStack> armorMap,
            Map<org.bukkit.inventory.EquipmentSlot, Boolean> blastProtectionMap,
            BotRegistry botRegistry) {
        UUID botUUID = botRegistry.getBotUUID(ownerUUID);
        if (botUUID == null) return;

        ITrainingBot bot = botRegistry.getBot(ownerUUID);
        if (bot == null || !bot.getUniqueId().equals(botUUID)) return;

        applyEquipment(bot, armorMap, blastProtectionMap);
        broadcastEquipment(bot, armorMap, blastProtectionMap);
    }
}
