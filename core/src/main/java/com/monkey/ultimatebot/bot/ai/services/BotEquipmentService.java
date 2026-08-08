package com.monkey.ultimatebot.bot.ai.services;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.nms.NMSBridgeManager;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class BotEquipmentService {

    private final ITrainingBot bot;

    public BotEquipmentService(ITrainingBot bot) {
        this.bot = bot;
    }

    public boolean handleDamage(float amount, EntityDamageEvent event) {
        try {
            boolean result = NMSBridgeManager.get().actuallyHurt(bot.asBukkitPlayer(), amount, event);
            if (result) applyArmorFix();
            return result;
        } catch (ClassCastException | NullPointerException e) {
            if (event != null && !event.isCancelled()) {
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
            if (armorPiece == null || armorPiece.isEmpty()) {
                continue;
            }
            ItemMeta meta = armorPiece.getItemMeta();
            if (meta instanceof Damageable damageable && damageable.hasDamage()) {
                damageable.setDamage(0);
                armorPiece.setItemMeta(meta);
                bot.setItem(slot, armorPiece);
            }
        }
    }
}
