package com.monkey.ultimatebot.combat.mode;

import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.common.model.CombatMode;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;

final class TridentPvPStrategy extends AbstractCombatModeStrategy {
    private static final int WATER_SLOT = BotInventoryController.TOTEM_SLOT;
    private static final int WEB_SLOT = BotInventoryController.OBSIDIAN_SLOT;
    private static final int SPONGE_SLOT = BotInventoryController.CRYSTAL_SLOT;
    private static final int RIPTIDE_COOLDOWN_TICKS = 60;
    private static final int TRIDENT_DRAW_TICKS = 10;

    private final TridentUtilityActions utilityActions = new TridentUtilityActions();
    private Phase phase = Phase.SELECT_ATTACK;
    private int phaseTicks;
    private long nextRiptideTick;

    TridentPvPStrategy() {
        super(
                CombatMode.TRIDENT,
                ModeKit.builder()
                        .slot(BotInventoryController.SWORD_SLOT, Items.TRIDENT)
                        .slot(WATER_SLOT, Items.WATER_BUCKET, 4)
                        .slot(WEB_SLOT, Items.COBWEB, 16)
                        .slot(SPONGE_SLOT, Items.SPONGE, 16)
                        .slot(BotInventoryController.GOLDEN_APPLE_SLOT, Items.GOLDEN_APPLE, 64)
                        .build());
    }

    @Override
    public void enter(CombatModeContext context) {
        super.enter(context);
        utilityActions.reset();
        nextRiptideTick = 0L;
        transitionTo(Phase.SELECT_ATTACK);
    }

    @Override
    public void exit(CombatModeContext context) {
        utilityActions.restoreWater(context);
        utilityActions.restoreSponge(context);
        super.exit(context);
    }

    @Override
    protected void execute(CombatModeContext context, LivingEntity target) {
        context.motion().aimAt(target);
        phaseTicks++;
        switch (phase) {
            case SELECT_ATTACK -> selectAttack(context, target);
            case PREPARE_RIPTIDE -> prepareRiptide(context);
            case RIPTIDE -> riptide(context, target);
            case AIR_HIT -> airHit(context, target);
            case DRAW_TRIDENT -> drawTrident(context, target);
            case THROW_RECOVERY -> throwRecovery(context, target);
            case MELEE -> melee(context, target);
            case TRAP -> trap(context, target);
            case RECOVER -> recover(context, target);
        }
    }

    private void selectAttack(CombatModeContext context, LivingEntity target) {
        double distance = context.motion().distanceTo(target);
        if (context.motion().isBotInWater()) {
            transitionTo(Phase.RIPTIDE);
        } else if (currentTick() >= nextRiptideTick
                && distance >= 3.5D
                && distance <= 12.0D
                && context.random().nextDouble() < 0.35D) {
            transitionTo(Phase.PREPARE_RIPTIDE);
        } else if (distance >= 3.8D) {
            transitionTo(Phase.DRAW_TRIDENT);
        } else {
            transitionTo(Phase.MELEE);
        }
    }

    private void prepareRiptide(CombatModeContext context) {
        if (phaseTicks == 1) {
            utilityActions.selectWater(context);
        } else if (phaseTicks == 2) {
            utilityActions.placeWater(context);
        }
        if (phaseTicks >= 4) {
            nextRiptideTick = currentTick() + RIPTIDE_COOLDOWN_TICKS;
            transitionTo(utilityActions.isWaterPrepared() ? Phase.RIPTIDE : Phase.DRAW_TRIDENT);
        }
    }

    private void riptide(CombatModeContext context, LivingEntity target) {
        context.inventory().switchToSlot(BotInventoryController.SWORD_SLOT);
        context.motion().setSwimming(true);
        if (phaseTicks == 1) {
            utilityActions.restoreWater(context);
            context.motion().propelTowards(target, 0.72D, riptideVerticalVelocity(context, target));
        } else {
            context.motion().steerVelocityTowards(target, 0.42D, context.bot().getDeltaMovement().y);
        }
        if (context.motion().distanceTo(target) <= 3.1D || phaseTicks >= 7) {
            context.motion().setSwimming(false);
            transitionTo(Phase.AIR_HIT);
        }
    }

    private void airHit(CombatModeContext context, LivingEntity target) {
        context.inventory().switchToSlot(BotInventoryController.SWORD_SLOT);
        context.motion().steerVelocityTowards(target, 0.34D, context.bot().getDeltaMovement().y);
        if (context.motion().distanceTo(target) <= context.tuning().attackRange()) {
            context.actions().attack(target, BotInventoryController.SWORD_SLOT);
            transitionTo(Phase.RECOVER);
        } else if (phaseTicks >= 5) {
            transitionTo(Phase.DRAW_TRIDENT);
        }
    }

    private void drawTrident(CombatModeContext context, LivingEntity target) {
        if (phaseTicks == 1) {
            context.inventory().switchToSlot(BotInventoryController.SWORD_SLOT);
            context.actions().useMainhandItem();
        }
        context.motion().retreat(target, 5.0D);
        if (phaseTicks >= TRIDENT_DRAW_TICKS) {
            context.actions().releaseUseItem();
            context.projectiles().fireTrident(target, context.tuning().aimAccuracy());
            transitionTo(Phase.THROW_RECOVERY);
        }
    }

    private void throwRecovery(CombatModeContext context, LivingEntity target) {
        context.motion().strafe(target, 1.1D);
        if (phaseTicks >= 5) {
            boolean trapOpportunity = context.motion().distanceTo(target) <= 5.0D
                    && context.random().nextDouble() < 0.3D;
            transitionTo(trapOpportunity ? Phase.TRAP : Phase.RECOVER);
        }
    }

    private void melee(CombatModeContext context, LivingEntity target) {
        context.actions().releaseUseItem();
        double distance = context.motion().distanceTo(target);
        if (distance <= context.tuning().attackRange()) {
            context.actions().attack(target, BotInventoryController.SWORD_SLOT);
            transitionTo(Phase.RECOVER);
        } else {
            context.motion().approach(target, 2.3D);
        }
        if (phaseTicks >= 8) {
            transitionTo(Phase.DRAW_TRIDENT);
        }
    }

    private void trap(CombatModeContext context, LivingEntity target) {
        if (phaseTicks == 1) {
            utilityActions.placeWeb(context, target);
        } else if (phaseTicks == 3) {
            utilityActions.placeSponge(context, target);
        }
        context.motion().retreat(target, 5.0D);
        if (phaseTicks >= 7) {
            utilityActions.restoreSponge(context);
            transitionTo(Phase.RECOVER);
        }
    }

    private void recover(CombatModeContext context, LivingEntity target) {
        context.inventory().switchToSlot(BotInventoryController.SWORD_SLOT);
        context.motion().strafe(target, 1.2D);
        if (phaseTicks >= 5) {
            transitionTo(Phase.SELECT_ATTACK);
        }
    }

    private double riptideVerticalVelocity(CombatModeContext context, LivingEntity target) {
        return Math.clamp((target.getY() - context.bot().getY()) * 0.22D + 0.24D, 0.12D, 0.52D);
    }

    private void transitionTo(Phase nextPhase) {
        phase = nextPhase;
        phaseTicks = 0;
    }

    private enum Phase {
        SELECT_ATTACK,
        PREPARE_RIPTIDE,
        RIPTIDE,
        AIR_HIT,
        DRAW_TRIDENT,
        THROW_RECOVERY,
        MELEE,
        TRAP,
        RECOVER
    }
}
