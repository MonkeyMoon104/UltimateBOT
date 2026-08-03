package com.monkey.ultimatebot.combat.mode.trident;

import com.monkey.ultimatebot.combat.mode.runtime.AbstractCombatModeStrategy;
import com.monkey.ultimatebot.combat.mode.runtime.CombatModeContext;
import com.monkey.ultimatebot.combat.mode.shared.CobwebCombatAwareness;
import com.monkey.ultimatebot.common.model.CombatMode;
import net.minecraft.world.entity.LivingEntity;

public final class TridentPvPStrategy extends AbstractCombatModeStrategy {
    private static final int RIPTIDE_COOLDOWN_TICKS = 70;
    private static final int RIPTIDE_CHARGE_TICKS = 6;
    private static final int LOYALTY_DRAW_TICKS = 9;

    private final TridentUtilityActions utilityActions = new TridentUtilityActions();
    private final TridentSpongeWebController spongeWebController = new TridentSpongeWebController();
    private Phase phase = Phase.SELECT_ATTACK;
    private int phaseTicks;
    private long nextRiptideTick;
    private long nextTrapTick;
    private boolean loyaltyDue;

    public TridentPvPStrategy() {
        super(CombatMode.TRIDENT, TridentLoadout.create());
    }

    @Override
    public void enter(CombatModeContext context) {
        super.enter(context);
        utilityActions.reset();
        spongeWebController.reset();
        nextRiptideTick = 0L;
        nextTrapTick = 0L;
        loyaltyDue = true;
        transitionTo(Phase.SELECT_ATTACK);
    }

    @Override
    public void exit(CombatModeContext context) {
        utilityActions.restoreWater(context);
        spongeWebController.reset();
        super.exit(context);
    }

    @Override
    protected void execute(CombatModeContext context, LivingEntity target) {
        context.motion().aimAt(target);
        phaseTicks++;
        switch (phase) {
            case SELECT_ATTACK -> selectAttack(context, target);
            case PREPARE_WATER -> prepareWater(context);
            case CHARGE_RIPTIDE -> chargeRiptide(context, target);
            case RIPTIDE_FLIGHT -> riptideFlight(context, target);
            case AIR_HIT -> airHit(context, target);
            case LOYALTY_DRAW -> loyaltyDraw(context, target);
            case LOYALTY_RECOVERY -> loyaltyRecovery(context, target);
            case GROUND_HIT -> groundHit(context, target);
            case HOE_SWAP -> hoeSwap(context, target);
            case SPONGE_WEB -> spongeWeb(context, target);
            case RECOVER -> recover(context, target);
        }
    }

    private void selectAttack(CombatModeContext context, LivingEntity target) {
        double distance = context.motion().distanceTo(target);
        if (loyaltyDue && distance >= 3.5D) {
            transitionTo(Phase.LOYALTY_DRAW);
        } else if (context.motion().isBotInWater() && currentTick() >= nextRiptideTick) {
            transitionTo(Phase.CHARGE_RIPTIDE);
        } else if (currentTick() >= nextRiptideTick && distance >= 2.5D && distance <= 14.0D) {
            transitionTo(Phase.PREPARE_WATER);
        } else if (distance >= 5.0D) {
            transitionTo(Phase.LOYALTY_DRAW);
        } else {
            transitionTo(Phase.GROUND_HIT);
        }
    }

    private void prepareWater(CombatModeContext context) {
        if (phaseTicks == 1) {
            utilityActions.selectWater(context);
        } else if (phaseTicks == 2) {
            utilityActions.placeWater(context);
        }
        if (phaseTicks >= 5) {
            nextRiptideTick = currentTick() + RIPTIDE_COOLDOWN_TICKS;
            transitionTo(utilityActions.isWaterPrepared() ? Phase.CHARGE_RIPTIDE : Phase.LOYALTY_DRAW);
        }
    }

    private void chargeRiptide(CombatModeContext context, LivingEntity target) {
        context.inventory().switchToSlot(TridentLoadout.RIPTIDE_SLOT);
        if (phaseTicks == 1) {
            context.actions().useMainhandItem();
        }
        context.motion().stop();
        if (phaseTicks >= RIPTIDE_CHARGE_TICKS) {
            context.actions().releaseUseItem();
            context.motion().setSwimming(true);
            context.motion().propelTowards(target, 0.75D, utilityActions.riptideVerticalVelocity(context, target));
            transitionTo(Phase.RIPTIDE_FLIGHT);
        }
    }

    private void riptideFlight(CombatModeContext context, LivingEntity target) {
        if (phaseTicks == 2) {
            utilityActions.restoreWater(context);
        }
        context.motion().steerVelocityTowards(target, 0.46D, context.bot().getDeltaMovement().y);
        if (context.motion().distanceTo(target) <= 3.0D || phaseTicks >= 8) {
            context.motion().setSwimming(false);
            transitionTo(Phase.AIR_HIT);
        }
    }

    private void airHit(CombatModeContext context, LivingEntity target) {
        context.inventory().switchToSlot(TridentLoadout.RIPTIDE_SLOT);
        context.motion().steerVelocityTowards(target, 0.34D, context.bot().getDeltaMovement().y);
        if (context.motion().distanceTo(target) <= context.tuning().attackRange()) {
            context.actions().attack(target, TridentLoadout.RIPTIDE_SLOT);
            loyaltyDue = true;
            transitionTo(Phase.RECOVER);
        } else if (phaseTicks >= 5) {
            transitionTo(Phase.LOYALTY_DRAW);
        }
    }

    private void loyaltyDraw(CombatModeContext context, LivingEntity target) {
        context.inventory().switchToSlot(TridentLoadout.LOYALTY_SLOT);
        if (phaseTicks == 1) {
            context.actions().useMainhandItem();
        }
        context.motion().retreat(target, 6.0D);
        if (phaseTicks >= LOYALTY_DRAW_TICKS) {
            context.actions().releaseUseItem();
            context.projectiles().fireTrident(target, context.tuning().aimAccuracy());
            loyaltyDue = false;
            transitionTo(Phase.LOYALTY_RECOVERY);
        }
    }

    private void loyaltyRecovery(CombatModeContext context, LivingEntity target) {
        context.motion().strafe(target, 0.7D);
        if (phaseTicks >= 6) {
            transitionTo(Phase.SELECT_ATTACK);
        }
    }

    private void groundHit(CombatModeContext context, LivingEntity target) {
        double distance = context.motion().distanceTo(target);
        if (distance > context.tuning().attackRange()) {
            context.motion().approach(target, 2.2D);
            if (phaseTicks >= 8) {
                transitionTo(Phase.SELECT_ATTACK);
            }
            return;
        }
        context.actions().attack(target, TridentLoadout.RIPTIDE_SLOT);
        transitionTo(Phase.HOE_SWAP);
    }

    private void hoeSwap(CombatModeContext context, LivingEntity target) {
        if (phaseTicks == 1) {
            context.inventory().switchToSlot(TridentLoadout.HOE_SLOT);
            context.motion().knockAway(target, 0.48D, 0.16D);
            context.actions().swingMainHand();
        }
        if (phaseTicks >= 3) {
            boolean trapOpportunity = currentTick() >= nextTrapTick
                    && context.motion().distanceTo(target) <= 4.5D
                    && !CobwebCombatAwareness.inspect(target).inside()
                    && context.random().nextDouble() < 0.35D + context.tuning().aggression() * 0.45D;
            if (trapOpportunity) {
                nextTrapTick = currentTick() + Math.max(30L, context.tuning().specialActionCooldownTicks());
                transitionTo(Phase.SPONGE_WEB);
            } else {
                transitionTo(Phase.LOYALTY_DRAW);
            }
        }
    }

    private void spongeWeb(CombatModeContext context, LivingEntity target) {
        if (context.motion().distanceTo(target) < 3.5D) {
            context.motion().retreat(target, 4.5D);
        } else {
            context.motion().strafe(target, 0.45D);
        }
        if (spongeWebController.tick(context, target, phaseTicks)) {
            spongeWebController.reset();
            transitionTo(Phase.LOYALTY_DRAW);
        }
    }

    private void recover(CombatModeContext context, LivingEntity target) {
        context.inventory().switchToSlot(TridentLoadout.RIPTIDE_SLOT);
        context.motion().strafe(target, 0.8D);
        if (phaseTicks >= 5) {
            transitionTo(Phase.SELECT_ATTACK);
        }
    }

    private void transitionTo(Phase nextPhase) {
        phase = nextPhase;
        phaseTicks = 0;
    }

    private enum Phase {
        SELECT_ATTACK,
        PREPARE_WATER,
        CHARGE_RIPTIDE,
        RIPTIDE_FLIGHT,
        AIR_HIT,
        LOYALTY_DRAW,
        LOYALTY_RECOVERY,
        GROUND_HIT,
        HOE_SWAP,
        SPONGE_WEB,
        RECOVER
    }
}
