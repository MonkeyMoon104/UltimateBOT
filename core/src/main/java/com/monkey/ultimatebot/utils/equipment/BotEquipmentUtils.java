package com.monkey.ultimatebot.utils.equipment;

import com.monkey.ultimatebot.common.model.EquipmentSlotKind;
import com.monkey.ultimatebot.bot.BotRegistry;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.compat.EnchantmentAccess;
import com.monkey.ultimatebot.compat.EquipmentSlotAccess;
import com.monkey.ultimatebot.compat.ItemMetaAccess;
import com.monkey.ultimatebot.nms.NMSBridgeManager;
import java.util.*;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;
import org.jspecify.annotations.Nullable;

public class BotEquipmentUtils {
    /**
     * Resolves enchantments without linking {@code NamespacedKey} or missing Bukkit enum fields.
     *
     * <p>{@code Enchantment#getByKey} / {@code NamespacedKey} crash 1.11 and earlier. Dual-path
     * lookup uses legacy names ({@code PROTECTION_ENVIRONMENTAL}) then modern keys.
     */
    public static @Nullable Enchantment resolveEnchantmentByKeyMinecraft(String key) {
        return EnchantmentAccess.byMinecraftKey(key);
    }

    public static void applyEquipment(
            ITrainingBot bot,
            Map<EquipmentSlotKind, org.bukkit.inventory.ItemStack> armorMap,
            Map<EquipmentSlotKind, Boolean> blastProtectionMap) {
        for (java.util.Map.Entry<EquipmentSlotKind, org.bukkit.inventory.ItemStack> entry : armorMap.entrySet()) {
            EquipmentSlotKind slot = entry.getKey();
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
            Enchantment blastProtection = resolveEnchantmentByKeyMinecraft("blast_protection");
            Enchantment protection = resolveEnchantmentByKeyMinecraft("protection");

            if (blastProtection != null) meta.removeEnchant(blastProtection);
            if (protection != null) meta.removeEnchant(protection);

            if (hasBlastProtection) {
                // Prefer blast protection, but safely fall back to normal protection (or no enchant).
                Enchantment toApply = blastProtection != null ? blastProtection : protection;
                if (toApply != null) meta.addEnchant(toApply, 4, false);
            } else {
                if (protection != null) meta.addEnchant(protection, 4, false);
            }

            // Prevent durability ticks from re-equipping armor (armor equip sound spam on hit).
            ItemMetaAccess.setUnbreakable(meta, true);

            item.setItemMeta(meta);
        }
    }

    public static void broadcastEquipment(
            ITrainingBot bot,
            Map<EquipmentSlotKind, org.bukkit.inventory.ItemStack> armorMap,
            Map<EquipmentSlotKind, Boolean> blastProtectionMap) {
        Map<EquipmentSlotKind, org.bukkit.inventory.ItemStack> equipment = new EnumMap<>(
                EquipmentSlotKind.class);

        for (java.util.Map.Entry<EquipmentSlotKind, org.bukkit.inventory.ItemStack> entry : armorMap.entrySet()) {
            EquipmentSlotKind slot = entry.getKey();
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
        Map<EquipmentSlotKind, org.bukkit.inventory.ItemStack> equipment = new EnumMap<>(
                EquipmentSlotKind.class);

        equipment.put(EquipmentSlotKind.HAND, bot.getItem(EquipmentSlotKind.HAND));
        EquipmentSlotKind offHand = EquipmentSlotAccess.offHand();
        if (offHand != null) {
            equipment.put(offHand, bot.getItem(offHand));
        }
        for (EquipmentSlotKind slot : EquipmentConverter.getArmorSlots()) {
            equipment.put(slot, bot.getItem(slot));
        }

        NMSBridgeManager.get().sendEquipment(viewer, bot, equipment);
    }

    public static @Nullable Map<EquipmentSlotKind, org.bukkit.inventory.ItemStack> getBotArmor(
            UUID playerUUID, BotRegistry botRegistry) {
        UUID botUUID = botRegistry.getBotUUID(playerUUID);
        if (botUUID == null) return null;

        ITrainingBot bot = botRegistry.getBot(playerUUID);
        if (bot == null || !bot.getUniqueId().equals(botUUID)) return null;

        Map<EquipmentSlotKind, org.bukkit.inventory.ItemStack> armor =
                new EnumMap<>(EquipmentSlotKind.class);

        for (EquipmentSlotKind slot : EquipmentConverter.getArmorSlots()) {
            armor.put(slot, bot.getItem(slot));
        }

        return armor;
    }

    public static void updateBotArmor(
            UUID ownerUUID,
            Map<EquipmentSlotKind, org.bukkit.inventory.ItemStack> armorMap,
            Map<EquipmentSlotKind, Boolean> blastProtectionMap,
            BotRegistry botRegistry) {
        UUID botUUID = botRegistry.getBotUUID(ownerUUID);
        if (botUUID == null) return;

        ITrainingBot bot = botRegistry.getBot(ownerUUID);
        if (bot == null || !bot.getUniqueId().equals(botUUID)) return;

        applyEquipment(bot, armorMap, blastProtectionMap);
        broadcastEquipment(bot, armorMap, blastProtectionMap);
    }
}
