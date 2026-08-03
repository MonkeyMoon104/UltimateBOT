package com.monkey.ultimatebot.bot.ai.services;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.nms.NMSBridgeManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.event.entity.EntityDamageEvent;

public class BotEquipmentService {

    private final ITrainingBot bot;

    public BotEquipmentService(ITrainingBot bot) {
        this.bot = bot;
    }

    public boolean handleDamage(ServerLevel level, DamageSource source, float amount, EntityDamageEvent event) {
        try {
            boolean result = NMSBridgeManager.get().actuallyHurt(bot.asPlayer(), level, source, amount, event);
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
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                ItemStack armorPiece = bot.asPlayer().getItemBySlot(slot);
                if (armorPiece != null && !armorPiece.isEmpty() && armorPiece.isDamageableItem()) {
                    armorPiece.setDamageValue(0);
                    bot.asPlayer().setItemSlot(slot, armorPiece);
                }
            }
        }
    }
}
