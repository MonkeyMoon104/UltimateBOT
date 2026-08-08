package com.monkey.ultimatebot.combat.mode.cart;

import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.combat.mode.runtime.CombatModeContext;
import org.bukkit.entity.LivingEntity;

final class CartBowController {
    static final int BOW_SLOT = BotInventoryController.ENDERPEARL_SLOT;
    static final int ARROW_SLOT = BotInventoryController.EMPTY_SLOT;

    private static final int FULL_DRAW_TICKS = 20;

    private int ignitionChargeTicks;

    boolean drawDistanceArrow(CombatModeContext context, LivingEntity target, int drawTicks) {
        selectAndDraw(context, drawTicks);
        if (context.motion().distanceTo(target) < 5.0D) {
            context.motion().retreat(target, 6.5D);
        } else {
            context.motion().strafe(target, 0.35D);
        }
        return drawTicks >= FULL_DRAW_TICKS;
    }

    boolean fireDistanceArrow(CombatModeContext context, LivingEntity target) {
        context.actions().releaseUseItem();
        if (!context.inventory().consumeItem(ARROW_SLOT)) {
            return false;
        }
        context.projectiles().fireArrow(target, Math.min(1.0D, context.tuning().aimAccuracy() + 0.08D));
        return true;
    }

    DrawStatus drawIgnitionArrow(
            CombatModeContext context, LivingEntity target, CartExplosiveSequence sequence, int drawTicks) {
        if (!sequence.isActive() || !sequence.isTargetCloseForIgnition(target)) {
            return DrawStatus.ABORT;
        }
        selectAndDraw(context, drawTicks);
        context.motion().stop();
        if (drawTicks < sequence.requiredIgnitionDrawTicks(context)) {
            return DrawStatus.CHARGING;
        }
        ignitionChargeTicks = drawTicks;
        return DrawStatus.READY;
    }

    boolean fireIgnitionArrow(CombatModeContext context, LivingEntity target, CartExplosiveSequence sequence) {
        context.actions().releaseUseItem();
        return sequence.isTargetCloseForIgnition(target)
                && context.inventory().consumeItem(ARROW_SLOT)
                && sequence.fireIgnitionArrow(context, context.tuning().aimAccuracy(), ignitionChargeTicks);
    }

    void reset() {
        ignitionChargeTicks = 0;
    }

    private static void selectAndDraw(CombatModeContext context, int drawTicks) {
        context.inventory().switchToSlot(BOW_SLOT);
        if (drawTicks == 1) {
            context.actions().useMainhandItem();
        }
    }

    enum DrawStatus {
        CHARGING,
        READY,
        ABORT
    }
}
