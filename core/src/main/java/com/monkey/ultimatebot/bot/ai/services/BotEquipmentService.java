package com.monkey.ultimatebot.bot.ai.services;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.compat.ItemMetaDamageAccess;
import com.monkey.ultimatebot.compat.ItemStackAccess;
import com.monkey.ultimatebot.nms.NMSBridgeManager;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jspecify.annotations.Nullable;

public class BotEquipmentService {

    private final ITrainingBot bot;

    public BotEquipmentService(ITrainingBot bot) {
        this.bot = bot;
    }

    public boolean handleDamage(float amount, @Nullable EntityDamageEvent event) {
        try {
            if (event == null) {
                // NMS actuallyHurt can run before Bukkit attaches lastDamageCause.
                applyArmorFix();
                return true;
            }
            boolean result = NMSBridgeManager.get().actuallyHurt(bot.asBukkitPlayer(), amount, event);
            if (result) applyArmorFix();
            return result;
        } catch (ClassCastException | NullPointerException e) {
            if (event == null || !event.isCancelled()) {
                applyArmorFix();
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private void applyArmorFix() {
        for (EquipmentSlot slot : new EquipmentSlot[] {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
        }) {
            ItemStack armorPiece = bot.getItem(slot);
            if (armorPiece == null || ItemStackAccess.isEmpty(armorPiece)) {
                continue;
            }
            ItemMeta meta = armorPiece.getItemMeta();
            if (meta == null) {
                continue;
            }
            boolean changed = false;
            try {
                if (!meta.isUnbreakable()) {
                    meta.setUnbreakable(true);
                    changed = true;
                }
            } catch (NoSuchMethodError ignored) {
                // pre-unbreakable API
            }
            if (ItemMetaDamageAccess.clearDamage(armorPiece, meta)) {
                changed = true;
            }
            // Re-equip only when durability actually changed; unbreakable stops repeat equip sounds.
            if (changed) {
                armorPiece.setItemMeta(meta);
                bot.setItem(slot, armorPiece);
            }
        }
    }
}
