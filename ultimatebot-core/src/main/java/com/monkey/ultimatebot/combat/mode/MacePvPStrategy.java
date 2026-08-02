package com.monkey.ultimatebot.combat.mode;

import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.common.model.CombatMode;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;

final class MacePvPStrategy extends AbstractCombatModeStrategy {
    MacePvPStrategy() {
        super(
                CombatMode.MACE,
                ModeKit.builder()
                        .slot(BotInventoryController.SWORD_SLOT, Items.MACE)
                        .slot(BotInventoryController.ENDERPEARL_SLOT, Items.WIND_CHARGE, 64)
                        .slot(BotInventoryController.OBSIDIAN_SLOT, Items.COBWEB, 64)
                        .slot(BotInventoryController.GOLDEN_APPLE_SLOT, Items.GOLDEN_APPLE, 64)
                        .build());
    }

    @Override
    protected void execute(CombatModeContext context, LivingEntity target) {
        context.aimAt(target);
        double distance = context.distanceTo(target);
        if (distance >= 4.0D
                && distance <= 12.0D
                && specialActionReady()
                && context.inventory().consumeItem(BotInventoryController.ENDERPEARL_SLOT)) {
            context.inventory().switchToSlot(BotInventoryController.ENDERPEARL_SLOT);
            context.projectiles().fireWindCharge(target, context.tuning().aimAccuracy());
            context.bot().setDeltaMovement(context.bot().getDeltaMovement().add(0.0D, 0.62D, 0.0D));
            delaySpecialAction(context);
            return;
        }
        if (distance <= context.tuning().attackRange() && context.bot().getDeltaMovement().y < -0.08D) {
            context.attack(target, BotInventoryController.SWORD_SLOT);
            return;
        }
        meleeOrMove(context, target, BotInventoryController.SWORD_SLOT);
    }
}
