package com.monkey.ultimatebot.combat.mode;

import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.common.model.CombatMode;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;

final class CartPvPStrategy extends AbstractCombatModeStrategy {
    private static final int BOW_SLOT = BotInventoryController.ENDERPEARL_SLOT;
    private static final int RAIL_SLOT = BotInventoryController.OBSIDIAN_SLOT;
    private static final int CART_SLOT = BotInventoryController.CRYSTAL_SLOT;
    private static final int ARROW_SLOT = BotInventoryController.EMPTY_SLOT;
    private static final int BOW_DRAW_TICKS = 12;

    private final CartExplosiveSequence explosiveSequence = new CartExplosiveSequence();
    private Phase phase = Phase.MELEE;
    private int phaseTicks;

    CartPvPStrategy() {
        super(
                CombatMode.CART,
                ModeKit.builder()
                        .slot(BotInventoryController.SWORD_SLOT, Items.NETHERITE_SWORD)
                        .slot(BOW_SLOT, Items.BOW)
                        .slot(RAIL_SLOT, Items.RAIL, 64)
                        .slot(CART_SLOT, Items.TNT_MINECART, 64)
                        .slot(ARROW_SLOT, Items.ARROW, 64)
                        .build());
    }

    @Override
    public void enter(CombatModeContext context) {
        super.enter(context);
        explosiveSequence.cleanup(context);
        transitionTo(Phase.MELEE);
    }

    @Override
    public void exit(CombatModeContext context) {
        explosiveSequence.cleanup(context);
        super.exit(context);
    }

    @Override
    protected void execute(CombatModeContext context, LivingEntity target) {
        context.motion().aimAt(target);
        phaseTicks++;
        if (explosiveSequence.isActive()) {
            CartExplosiveSequence.Status status = explosiveSequence.tick(context);
            if (status != CartExplosiveSequence.Status.ACTIVE) {
                transitionTo(Phase.RECOVER);
            }
        }
        switch (phase) {
            case MELEE -> melee(context, target);
            case CREATE_DISTANCE -> createDistance(context, target);
            case DISTANCE_DRAW -> drawBow(context, target, Phase.DISTANCE_FIRE);
            case DISTANCE_FIRE -> fireDistanceArrow(context, target);
            case PLACE_RAIL -> placeRail(context, target);
            case PLACE_CART -> placeCart(context);
            case EVADE -> evade(context, target);
            case IGNITION_DRAW -> drawBow(context, target, Phase.IGNITION_FIRE);
            case IGNITION_FIRE -> fireCartArrow(context);
            case WAIT_IMPACT -> waitForImpact(context, target);
            case RECOVER -> recover(context, target);
        }
    }

    private void melee(CombatModeContext context, LivingEntity target) {
        double distance = context.motion().distanceTo(target);
        boolean opportunity = ModeCombatPolicy.isCartOpportunity(
                distance,
                context.bot().getY(),
                target.getY(),
                context.actions().targetHealthRatio(target),
                currentTick() >= Math.max(10L, context.tuning().reactionTicks() * 2L));
        if (opportunity && specialActionReady()) {
            transitionTo(Phase.CREATE_DISTANCE);
        } else {
            meleeOrMove(context, target, BotInventoryController.SWORD_SLOT);
        }
    }

    private void createDistance(CombatModeContext context, LivingEntity target) {
        if (context.motion().distanceTo(target) < 5.5D && phaseTicks < 12) {
            context.motion().retreat(target, 6.5D);
            return;
        }
        transitionTo(Phase.DISTANCE_DRAW);
    }

    private void drawBow(CombatModeContext context, LivingEntity target, Phase releasePhase) {
        context.inventory().switchToSlot(BOW_SLOT);
        if (phaseTicks == 1) {
            context.actions().useMainhandItem();
        }
        if (context.motion().distanceTo(target) < 5.0D) {
            context.motion().retreat(target, 6.5D);
        } else {
            context.motion().strafe(target, 0.35D);
        }
        if (phaseTicks >= BOW_DRAW_TICKS) {
            transitionTo(releasePhase);
        }
    }

    private void fireDistanceArrow(CombatModeContext context, LivingEntity target) {
        if (phaseTicks == 1) {
            context.actions().releaseUseItem();
            if (!context.inventory().consumeItem(ARROW_SLOT)) {
                abort(context);
                return;
            }
            context.projectiles()
                    .fireArrow(target, Math.min(1.0D, context.tuning().aimAccuracy() + 0.08D));
        }
        if (phaseTicks >= 3) {
            transitionTo(Phase.PLACE_RAIL);
        }
    }

    private void placeRail(CombatModeContext context, LivingEntity target) {
        if (!explosiveSequence.placeRail(context, target)) {
            abort(context);
            return;
        }
        transitionTo(Phase.PLACE_CART);
    }

    private void placeCart(CombatModeContext context) {
        if (!explosiveSequence.placeCart(context)) {
            abort(context);
            return;
        }
        delaySpecialAction(context);
        transitionTo(Phase.EVADE);
    }

    private void evade(CombatModeContext context, LivingEntity target) {
        context.motion().retreat(target, 7.0D);
        if (context.motion().distanceTo(target) >= 6.0D || phaseTicks >= 10) {
            transitionTo(Phase.IGNITION_DRAW);
        }
    }

    private void fireCartArrow(CombatModeContext context) {
        if (phaseTicks == 1) {
            context.actions().releaseUseItem();
            if (!context.inventory().consumeItem(ARROW_SLOT)
                    || !explosiveSequence.fireIgnitionArrow(
                            context, context.tuning().aimAccuracy())) {
                abort(context);
                return;
            }
        }
        transitionTo(Phase.WAIT_IMPACT);
    }

    private void waitForImpact(CombatModeContext context, LivingEntity target) {
        context.motion().retreat(target, 8.0D);
        if (!explosiveSequence.isActive()) {
            transitionTo(Phase.RECOVER);
        }
    }

    private void recover(CombatModeContext context, LivingEntity target) {
        context.motion().strafe(target, 1.2D);
        if (phaseTicks >= 8) {
            transitionTo(Phase.MELEE);
        }
    }

    private void abort(CombatModeContext context) {
        context.actions().releaseUseItem();
        explosiveSequence.cleanup(context);
        delaySpecialAction(context);
        transitionTo(Phase.RECOVER);
    }

    private void transitionTo(Phase nextPhase) {
        phase = nextPhase;
        phaseTicks = 0;
    }

    private enum Phase {
        MELEE,
        CREATE_DISTANCE,
        DISTANCE_DRAW,
        DISTANCE_FIRE,
        PLACE_RAIL,
        PLACE_CART,
        EVADE,
        IGNITION_DRAW,
        IGNITION_FIRE,
        WAIT_IMPACT,
        RECOVER
    }
}
