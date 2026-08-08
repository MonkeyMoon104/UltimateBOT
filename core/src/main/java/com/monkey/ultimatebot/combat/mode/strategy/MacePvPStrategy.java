package com.monkey.ultimatebot.combat.mode.strategy;

import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.combat.mode.runtime.AbstractCombatModeStrategy;
import com.monkey.ultimatebot.combat.mode.runtime.CombatModeContext;
import com.monkey.ultimatebot.combat.mode.runtime.ModeKit;
import com.monkey.ultimatebot.combat.mode.shared.ModeCombatPolicy;
import com.monkey.ultimatebot.common.model.CombatMode;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;

public final class MacePvPStrategy extends AbstractCombatModeStrategy {
    private static final int WIND_CHARGE_SLOT = BotInventoryController.ENDERPEARL_SLOT;

    private Phase phase = Phase.APPROACH;
    private int phaseTicks;

    public MacePvPStrategy() {
        super(
                CombatMode.MACE,
                ModeKit.builder()
                        .slot(BotInventoryController.SWORD_SLOT, Material.MACE)
                        .slot(BotInventoryController.ENDERPEARL_SLOT, Material.WIND_CHARGE, 64)
                        .slot(BotInventoryController.OBSIDIAN_SLOT, Material.COBWEB, 64)
                        .slot(BotInventoryController.GOLDEN_APPLE_SLOT, Material.GOLDEN_APPLE, 64)
                        .build());
    }

    @Override
    public void enter(CombatModeContext context) {
        super.enter(context);
        transitionTo(Phase.APPROACH);
    }

    @Override
    protected void execute(CombatModeContext context, LivingEntity target) {
        context.motion().aimAt(target);
        phaseTicks++;
        switch (phase) {
            case APPROACH -> approachLaunchWindow(context, target);
            case ASCEND -> ascendAboveTarget(context, target);
            case ALIGN -> alignForDive(context, target);
            case DIVE -> diveAndStrike(context, target);
            case RECOVER -> recover(context, target);
        }
    }

    private void approachLaunchWindow(CombatModeContext context, LivingEntity target) {
        double horizontalDistance = context.motion().horizontalDistanceTo(target);
        if (!context.motion().hasVerticalClearance(target, 5)) {
            if (context.motion().distanceTo(target) <= context.tuning().attackRange()) {
                context.actions().attack(target, BotInventoryController.SWORD_SLOT);
            } else {
                context.motion().approach(target, 1.8D);
            }
            return;
        }
        if (!context.motion().hasVerticalClearance(context.bukkitBot(), 6)) {
            context.motion().strafe(target, 2.2D);
            return;
        }
        if (horizontalDistance > 4.5D) {
            context.motion().approach(target, 3.4D);
            return;
        }
        if (!ModeCombatPolicy.canLaunchMace(horizontalDistance, context.motion().isBotOnGround(), specialActionReady())) {
            context.motion().strafe(target, 0.9D);
            return;
        }
        if (!context.inventory().consumeItem(WIND_CHARGE_SLOT)) {
            context.motion().retreat(target, 5.0D);
            return;
        }
        context.inventory().switchToSlot(WIND_CHARGE_SLOT);
        context.projectiles().launchSelfWindCharge();
        context.motion().propelTowards(target, 0.16D, 1.02D);
        delaySpecialAction(context);
        transitionTo(Phase.ASCEND);
    }

    private void ascendAboveTarget(CombatModeContext context, LivingEntity target) {
        if (!context.motion().hasVerticalClearance(context.bukkitBot(), 2)) {
            transitionTo(Phase.RECOVER);
            return;
        }
        double verticalVelocity = context.motion().botVerticalVelocity();
        context.motion().steerVelocityTowards(target, 0.22D, verticalVelocity);
        if (context.motion().heightAbove(target) >= 3.0D && verticalVelocity <= 0.12D) {
            transitionTo(Phase.ALIGN);
        } else if (phaseTicks > 22 || context.motion().isBotOnGround()) {
            transitionTo(Phase.RECOVER);
        }
    }

    private void alignForDive(CombatModeContext context, LivingEntity target) {
        double verticalVelocity = Math.min(context.motion().botVerticalVelocity(), -0.12D);
        context.motion()
                .steerVelocityTowards(
                        target, context.motion().horizontalDistanceTo(target) > 1.0D ? 0.28D : 0.08D, verticalVelocity);
        if (context.motion().horizontalDistanceTo(target) <= 1.35D || phaseTicks >= 6) {
            transitionTo(Phase.DIVE);
        }
    }

    private void diveAndStrike(CombatModeContext context, LivingEntity target) {
        context.motion()
                .steerVelocityTowards(target, 0.34D, Math.min(context.motion().botVerticalVelocity() - 0.04D, -0.36D));
        boolean strikeWindow = context.motion().heightAbove(target) <= 2.8D
                && context.motion().heightAbove(target) >= -0.2D
                && context.motion().horizontalDistanceTo(target) <= 1.75D;
        if (strikeWindow) {
            context.bukkitBot().setFallDistance(Math.max(context.bukkitBot().getFallDistance(), 6.0F));
            context.actions().attack(target, BotInventoryController.SWORD_SLOT);
            transitionTo(Phase.RECOVER);
        } else if (context.motion().isBotOnGround() || phaseTicks > 18) {
            transitionTo(Phase.RECOVER);
        }
    }

    private void recover(CombatModeContext context, LivingEntity target) {
        if (phaseTicks <= 7) {
            context.motion().retreat(target, 4.5D);
            return;
        }
        transitionTo(Phase.APPROACH);
    }

    private void transitionTo(Phase nextPhase) {
        phase = nextPhase;
        phaseTicks = 0;
    }

    private enum Phase {
        APPROACH,
        ASCEND,
        ALIGN,
        DIVE,
        RECOVER
    }
}
