package com.monkey.ultimatebot.combat.mode;

import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.common.model.CombatMode;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;

final class WaterPvPStrategy extends AbstractCombatModeStrategy {
    WaterPvPStrategy() {
        super(
                CombatMode.WATER,
                ModeKit.builder()
                        .slot(BotInventoryController.SWORD_SLOT, Items.TRIDENT)
                        .slot(BotInventoryController.ENDERPEARL_SLOT, Items.WATER_BUCKET)
                        .slot(BotInventoryController.TOTEM_SLOT, Items.DIAMOND_SWORD)
                        .slot(BotInventoryController.GOLDEN_APPLE_SLOT, Items.GOLDEN_APPLE, 64)
                        .build());
    }

    @Override
    protected void execute(CombatModeContext context, LivingEntity target) {
        context.aimAt(target);
        double distance = context.distanceTo(target);
        boolean inWater = context.bot().isEyeInFluid(FluidTags.WATER) || target.isEyeInFluid(FluidTags.WATER);
        if (distance >= 4.0D && distance <= 24.0D && specialActionReady()) {
            context.inventory().switchToSlot(BotInventoryController.SWORD_SLOT);
            context.projectiles().fireTrident(target, context.tuning().aimAccuracy() + (inWater ? 0.05D : 0.0D));
            delaySpecialAction(context);
            context.strafe(target, inWater ? 2.2D : 1.4D);
            return;
        }
        meleeOrMove(context, target, BotInventoryController.TOTEM_SLOT);
    }
}
