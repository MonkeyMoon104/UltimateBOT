package com.monkey.ultimatebot.combat.mode;

import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.common.model.CombatMode;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;

final class SwordPvPStrategy extends AbstractCombatModeStrategy {
    SwordPvPStrategy() {
        super(
                CombatMode.SWORD,
                ModeKit.builder()
                        .slot(BotInventoryController.SWORD_SLOT, Items.DIAMOND_SWORD)
                        .slot(BotInventoryController.ENDERPEARL_SLOT, Items.ENDER_PEARL, 16)
                        .slot(BotInventoryController.GOLDEN_APPLE_SLOT, Items.GOLDEN_APPLE, 64)
                        .build());
    }

    @Override
    protected void execute(CombatModeContext context, LivingEntity target) {
        context.aimAt(target);
        if (context.healthRatio() <= context.tuning().retreatHealthRatio()) {
            context.retreat(target, 5.5D);
            return;
        }
        meleeOrMove(context, target, BotInventoryController.SWORD_SLOT);
    }
}
