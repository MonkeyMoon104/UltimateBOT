package com.monkey.ultimatebot.combat.mode.uhc;

import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.combat.mode.runtime.CombatModeContext;
import net.minecraft.world.entity.LivingEntity;

final class UhcWebPressureController {
    void tick(CombatModeContext context, LivingEntity target) {
        double distance = context.motion().distanceTo(target);
        context.actions().releaseUseItem();
        context.inventory().switchToSlot(BotInventoryController.SWORD_SLOT);
        if (distance < 2.2D) {
            context.motion().retreat(target, 2.8D);
        } else if (distance > context.tuning().attackRange()) {
            context.motion().approach(target, Math.max(2.45D, context.tuning().attackRange() - 0.25D));
        } else {
            context.motion().strafe(target, 0.35D);
        }
        if (distance <= context.tuning().attackRange()) {
            context.actions().attack(target, BotInventoryController.SWORD_SLOT);
        }
    }

    void reset(CombatModeContext context) {
        context.actions().releaseUseItem();
    }
}
