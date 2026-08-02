package com.monkey.ultimatebot.combat.mode;

import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.common.model.CombatMode;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;

final class TridentPvPStrategy extends AbstractCombatModeStrategy {
    TridentPvPStrategy() {
        super(
                CombatMode.TRIDENT,
                ModeKit.builder()
                        .slot(BotInventoryController.SWORD_SLOT, Items.TRIDENT)
                        .slot(BotInventoryController.ENDERPEARL_SLOT, Items.ENDER_PEARL, 16)
                        .slot(BotInventoryController.TOTEM_SLOT, Items.DIAMOND_SWORD)
                        .slot(BotInventoryController.GOLDEN_APPLE_SLOT, Items.GOLDEN_APPLE, 64)
                        .build());
    }

    @Override
    protected void execute(CombatModeContext context, LivingEntity target) {
        context.aimAt(target);
        double distance = context.distanceTo(target);
        if (distance >= 4.0D
                && distance <= 30.0D
                && specialActionReady()
                && context.inventory().consumeItem(BotInventoryController.SWORD_SLOT)) {
            context.projectiles().fireTrident(target, context.tuning().aimAccuracy());
            delaySpecialAction(context);
            context.strafe(target, 1.8D);
            return;
        }
        meleeOrMove(context, target, BotInventoryController.TOTEM_SLOT);
    }
}
