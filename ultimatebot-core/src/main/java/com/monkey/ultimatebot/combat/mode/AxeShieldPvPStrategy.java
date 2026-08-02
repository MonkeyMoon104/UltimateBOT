package com.monkey.ultimatebot.combat.mode;

import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.common.model.CombatMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;

final class AxeShieldPvPStrategy extends AbstractCombatModeStrategy {
    AxeShieldPvPStrategy() {
        super(
                CombatMode.AXE_SHIELD,
                ModeKit.builder()
                        .slot(BotInventoryController.SWORD_SLOT, Items.NETHERITE_AXE)
                        .slot(BotInventoryController.ENDERPEARL_SLOT, Items.SHIELD)
                        .slot(BotInventoryController.GOLDEN_APPLE_SLOT, Items.GOLDEN_APPLE, 64)
                        .build());
    }

    @Override
    protected void execute(CombatModeContext context, LivingEntity target) {
        context.aimAt(target);
        double distance = context.distanceTo(target);
        boolean defend = distance <= 4.0D
                && context.random().nextDouble() < context.tuning().defensiveChance()
                && !context.bot().isUsingItem();
        if (defend) {
            context.inventory().switchToSlot(BotInventoryController.ENDERPEARL_SLOT);
            context.bot().startUsingItem(InteractionHand.MAIN_HAND);
            context.strafe(target, 1.2D);
            return;
        }
        if (context.bot().isUsingItem()) {
            context.bot().releaseUsingItem();
        }
        meleeOrMove(context, target, BotInventoryController.SWORD_SLOT);
    }
}
