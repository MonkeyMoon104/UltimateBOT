package com.monkey.ultimatebot.combat.mode.cart;

import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.combat.mode.runtime.AbstractCombatModeStrategy;
import com.monkey.ultimatebot.combat.mode.runtime.CombatModeContext;
import com.monkey.ultimatebot.combat.mode.runtime.ModeKit;
import com.monkey.ultimatebot.combat.mode.shared.ModeCombatPolicy;
import com.monkey.ultimatebot.common.model.combat.CombatMode;
import com.monkey.ultimatebot.access.entity.EntityCoordsAccess;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;

public final class CartPvPStrategy extends AbstractCombatModeStrategy {
    private static final int RAIL_SLOT = BotInventoryController.OBSIDIAN_SLOT;
    private static final int CART_SLOT = BotInventoryController.CRYSTAL_SLOT;

    private final CartBowController bowController = new CartBowController();
    private final CartExplosiveSequence explosiveSequence = new CartExplosiveSequence();
    private Phase phase = Phase.MELEE;
    private int phaseTicks;
    private long nextCartTick;

    public CartPvPStrategy() {
        super(
                CombatMode.CART,
                ModeKit.builder()
                        .slot(BotInventoryController.SWORD_SLOT, "NETHERITE_SWORD", Material.DIAMOND_SWORD, 1)
                        .slot(CartBowController.BOW_SLOT, Material.BOW)
                        .slot(RAIL_SLOT, "RAIL", Material.IRON_INGOT, 64)
                        .slot(CART_SLOT, "TNT_MINECART", Material.TNT, 64)
                        .slot(CartBowController.ARROW_SLOT, Material.ARROW, 64)
                        .build());
    }

    @Override
    public void enter(CombatModeContext context) {
        super.enter(context);
        bowController.reset();
        explosiveSequence.release(context);
        nextCartTick = 2L;
        transitionTo(Phase.MELEE);
    }

    @Override
    public void exit(CombatModeContext context) {
        explosiveSequence.release(context);
        super.exit(context);
    }

    @Override
    protected void execute(CombatModeContext context, LivingEntity target) {
        context.motion().aimAt(target);
        phaseTicks++;
        if (explosiveSequence.isActive()) {
            handleSequenceStatus(context, target, explosiveSequence.tick(context, target));
        }
        switch (phase) {
            case MELEE:
                melee(context, target);
                break;
            case CREATE_DISTANCE:
                createDistance(context, target);
                break;
            case DISTANCE_DRAW:
                drawDistanceBow(context, target);
                break;
            case DISTANCE_FIRE:
                fireDistanceArrow(context, target);
                break;
            case PLACE_RAIL:
                placeRail(context, target);
                break;
            case PLACE_CART:
                placeCart(context);
                break;
            case EVADE:
                evade(context, target);
                break;
            case IGNITION_DRAW:
                drawIgnitionBow(context, target);
                break;
            case IGNITION_FIRE:
                fireCartArrow(context, target);
                break;
            case WAIT_IMPACT:
                waitForImpact(context, target);
                break;
            case RECOVER:
                recover(context, target);
                break;
        }
    }

    private void melee(CombatModeContext context, LivingEntity target) {
        double distance = context.motion().distanceTo(target);
        boolean opportunity = ModeCombatPolicy.isCartOpportunity(
                distance,
                context.motion().botY(),
                EntityCoordsAccess.getY(target),
                context.actions().targetHealthRatio(target),
                currentTick() >= nextCartTick);
        if (opportunity && currentTick() >= nextCartTick) {
            transitionTo(distance < 2.9D ? Phase.CREATE_DISTANCE : Phase.PLACE_RAIL);
        } else {
            meleeOrMove(context, target, BotInventoryController.SWORD_SLOT);
        }
    }

    private void createDistance(CombatModeContext context, LivingEntity target) {
        double distance = context.motion().distanceTo(target);
        transitionTo(distance < 2.9D ? Phase.DISTANCE_DRAW : Phase.PLACE_RAIL);
    }

    private void drawDistanceBow(CombatModeContext context, LivingEntity target) {
        if (bowController.drawDistanceArrow(context, target, phaseTicks)) {
            transitionTo(Phase.DISTANCE_FIRE);
        }
    }

    private void drawIgnitionBow(CombatModeContext context, LivingEntity target) {
        switch (bowController.drawIgnitionArrow(context, target, explosiveSequence, phaseTicks)) {
            case CHARGING:
                return;
            case READY:
                transitionTo(Phase.IGNITION_FIRE);
                break;
            case ABORT:
                abort(context);
                break;
        }
    }

    private void fireDistanceArrow(CombatModeContext context, LivingEntity target) {
        if (phaseTicks == 1) {
            if (!bowController.fireDistanceArrow(context, target)) {
                abort(context);
                return;
            }
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
        nextCartTick = currentTick() + Math.max(4L, context.tuning().reactionTicks() / 2L);
        transitionTo(Phase.EVADE);
    }

    private void evade(CombatModeContext context, LivingEntity target) {
        if (!explosiveSequence.isTargetCloseForIgnition(target)) {
            abandonAndRetry(context);
            return;
        }
        if (context.motion().distanceTo(target) < 4.5D && phaseTicks < 4) {
            context.motion().retreat(target, 5.5D);
        } else {
            transitionTo(Phase.IGNITION_DRAW);
        }
    }

    private void fireCartArrow(CombatModeContext context, LivingEntity target) {
        if (phaseTicks == 1) {
            if (!bowController.fireIgnitionArrow(context, target, explosiveSequence)) {
                abort(context);
                return;
            }
        }
        transitionTo(Phase.WAIT_IMPACT);
    }

    private void waitForImpact(CombatModeContext context, LivingEntity target) {
        if (context.motion().distanceTo(target) < 4.5D) {
            context.motion().retreat(target, 5.5D);
        } else {
            context.motion().stop();
        }
        if (!explosiveSequence.isActive()) {
            transitionTo(Phase.RECOVER);
        }
    }

    private void recover(CombatModeContext context, LivingEntity target) {
        context.motion().strafe(target, 1.2D);
        if (phaseTicks >= 2) {
            transitionTo(Phase.MELEE);
        }
    }

    private void handleSequenceStatus(
            CombatModeContext context, LivingEntity target, CartExplosiveSequence.Status status) {
        switch (status) {
            case ACTIVE:
                return;
            case NEEDS_IGNITION:
                if (explosiveSequence.isTargetCloseForIgnition(target)) {
                    transitionTo(Phase.IGNITION_DRAW);
                    return;
                }
                abort(context);
                return;
            case TARGET_ESCAPED:
            case FINISHED:
                abandonAndRetry(context);
                break;
            case DETONATED:
                transitionTo(Phase.RECOVER);
                break;
        }
    }

    private void abort(CombatModeContext context) {
        context.actions().releaseUseItem();
        explosiveSequence.release(context);
        bowController.reset();
        nextCartTick = currentTick() + Math.max(2L, context.tuning().reactionTicks() / 2L);
        transitionTo(Phase.RECOVER);
    }

    private void abandonAndRetry(CombatModeContext context) {
        context.actions().releaseUseItem();
        explosiveSequence.release(context);
        bowController.reset();
        nextCartTick = currentTick() + 2L;
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
