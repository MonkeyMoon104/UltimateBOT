package com.monkey.ultimatebot.combat.mode;

import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import net.minecraft.world.entity.LivingEntity;

final class UhcWebPressureController {
    private static final int CROSSBOW_SLOT = BotInventoryController.TOTEM_SLOT;
    private static final int ARROW_SLOT = BotInventoryController.EMPTY_SLOT;
    private static final int DRAW_TICKS = 8;

    private int drawTicks;
    private int shotCooldownTicks;

    void tick(CombatModeContext context, LivingEntity target) {
        double distance = context.motion().distanceTo(target);
        if (distance < 4.5D) {
            context.motion().retreat(target, 6.0D);
        } else {
            context.motion().strafe(target, 0.55D);
        }
        context.inventory().switchToSlot(CROSSBOW_SLOT);
        if (shotCooldownTicks > 0) {
            shotCooldownTicks--;
            return;
        }
        if (drawTicks == 0) {
            context.actions().useMainhandItem();
        }
        drawTicks++;
        if (drawTicks < DRAW_TICKS) {
            return;
        }
        context.actions().releaseUseItem();
        if (context.inventory().consumeItem(ARROW_SLOT)) {
            context.projectiles().fireArrow(target, context.tuning().aimAccuracy());
        }
        drawTicks = 0;
        shotCooldownTicks = Math.max(8, context.tuning().reactionTicks() * 2);
    }

    void reset(CombatModeContext context) {
        if (drawTicks > 0) {
            context.actions().releaseUseItem();
        }
        drawTicks = 0;
        shotCooldownTicks = 0;
    }
}
