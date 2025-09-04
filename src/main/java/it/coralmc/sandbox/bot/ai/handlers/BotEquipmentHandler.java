package it.coralmc.sandbox.bot.ai.handlers;

import it.coralmc.sandbox.bot.ai.TrainingBot;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.event.entity.EntityDamageEvent;

public class BotEquipmentHandler {

    private final TrainingBot bot;

    public BotEquipmentHandler(TrainingBot bot) {
        this.bot = bot;
    }

    public boolean handleDamage(ServerLevel level, DamageSource source, float amount, EntityDamageEvent event) {
        try {
            boolean result = bot.callSuperActuallyHurt(level, source, amount, event);
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
                ItemStack armorPiece = bot.getItemBySlot(slot);
                if (armorPiece != null && !armorPiece.isEmpty() && armorPiece.isDamageableItem()) {
                    armorPiece.setDamageValue(0);
                    bot.setItemSlot(slot, armorPiece);
                }
            }
        }
    }
}
