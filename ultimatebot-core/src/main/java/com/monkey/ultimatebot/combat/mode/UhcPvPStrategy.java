package com.monkey.ultimatebot.combat.mode;

import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.common.model.CombatMode;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;

final class UhcPvPStrategy extends AbstractCombatModeStrategy {
    UhcPvPStrategy() {
        super(
                CombatMode.UHC,
                ModeKit.builder()
                        .slot(BotInventoryController.SWORD_SLOT, Items.DIAMOND_SWORD)
                        .slot(BotInventoryController.ENDERPEARL_SLOT, Items.BOW)
                        .slot(BotInventoryController.TOTEM_SLOT, Items.WATER_BUCKET)
                        .slot(BotInventoryController.OBSIDIAN_SLOT, Items.COBBLESTONE, 64)
                        .slot(BotInventoryController.GOLDEN_APPLE_SLOT, Items.GOLDEN_APPLE, 64)
                        .slot(BotInventoryController.EMPTY_SLOT, Items.ARROW, 64)
                        .build());
    }

    @Override
    protected void execute(CombatModeContext context, LivingEntity target) {
        context.aimAt(target);
        double distance = context.distanceTo(target);
        if (distance >= 5.0D
                && distance <= 28.0D
                && specialActionReady()
                && context.inventory().consumeItem(BotInventoryController.EMPTY_SLOT)) {
            context.inventory().switchToSlot(BotInventoryController.ENDERPEARL_SLOT);
            context.projectiles().fireArrow(target, context.tuning().aimAccuracy());
            delaySpecialAction(context);
            context.strafe(target, 1.5D);
            return;
        }
        meleeOrMove(context, target, BotInventoryController.SWORD_SLOT);
    }
}
