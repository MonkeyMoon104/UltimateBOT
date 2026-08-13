package com.monkey.ultimatebot.combat.mode.strategy;

import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.combat.mode.runtime.AbstractCombatModeStrategy;
import com.monkey.ultimatebot.combat.mode.runtime.CombatModeContext;
import com.monkey.ultimatebot.combat.mode.runtime.ModeKit;
import com.monkey.ultimatebot.combat.mode.shared.ModeCombatPolicy;
import com.monkey.ultimatebot.common.model.CombatMode;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;

public final class AxeShieldPvPStrategy extends AbstractCombatModeStrategy {
    private static final double GUARD_APPROACH_RANGE = 6.0D;

    private Phase phase = Phase.GUARD;
    private int guardedTicks;
    private int shieldHoldTicks;
    private int shieldRearmTicks;
    private boolean shieldRaised;

    public AxeShieldPvPStrategy() {
        super(
                CombatMode.AXE_SHIELD,
                ModeKit.builder()
                        .slot(BotInventoryController.SWORD_SLOT, Material.NETHERITE_AXE)
                        .slot(BotInventoryController.GOLDEN_APPLE_SLOT, Material.GOLDEN_APPLE, 64)
                        .equipment(EquipmentSlot.OFF_HAND, Material.SHIELD)
                        .build());
    }

    @Override
    public void enter(CombatModeContext context) {
        super.enter(context);
        guardedTicks = 0;
        shieldHoldTicks = 0;
        shieldRearmTicks = 0;
        shieldRaised = false;
        transitionTo(Phase.GUARD);
    }

    @Override
    protected void execute(CombatModeContext context, LivingEntity target) {
        context.actions().tickAttackCooldown();
        context.motion().aimAt(target);
        if (shieldRearmTicks > 0) {
            shieldRearmTicks--;
        }
                switch (phase) {
            case GUARD:
                guard(context, target);
                break;
            case AXE_STRIKE:
                axeStrike(context, target);
                break;
        }
    }

    private void guard(CombatModeContext context, LivingEntity target) {
        context.inventory().switchToSlot(BotInventoryController.SWORD_SLOT);
        double distance = context.motion().distanceTo(target);
        boolean shieldImpact = context.signals().consumeShieldImpact(target.getUniqueId());
        if (distance > GUARD_APPROACH_RANGE) {
            guardedTicks = 0;
            shieldHoldTicks = 0;
            lowerShield(context);
            context.motion().approach(target, 3.5D);
            return;
        }

        raiseShield(context);
        guardedTicks++;
        if (shieldImpact) {
            if (ModeCombatPolicy.shouldCounterShieldImpact(
                    distance,
                    context.tuning().attackRange(),
                    context.actions().canAttack(),
                    context.tuning().aggression(),
                    context.random().nextDouble())) {
                lowerShield(context);
                transitionTo(Phase.AXE_STRIKE);
                return;
            }
            shieldHoldTicks = Math.max(shieldHoldTicks, 4);
        }
        if (distance > context.tuning().attackRange()) {
            context.motion().approach(target, 2.6D);
            return;
        }

        boolean incomingAttack = context.actions().isIncomingAttackLikely(target);
        int maximumGuardTicks = Math.max(10, context.tuning().reactionTicks() * 3);
        if (incomingAttack && guardedTicks < maximumGuardTicks) {
            shieldHoldTicks = Math.max(shieldHoldTicks, 5);
            if (distance <= 1.85D) {
                context.motion().retreat(target, 2.8D);
            } else {
                context.motion().stop();
            }
            return;
        }

        if (shieldHoldTicks > 0) {
            shieldHoldTicks--;
            context.motion().stop();
            return;
        }
        if (ModeCombatPolicy.isSafeAxeOpening(
                distance,
                context.tuning().attackRange(),
                false,
                context.actions().canAttack(),
                guardedTicks)) {
            lowerShield(context);
            transitionTo(Phase.AXE_STRIKE);
        }
    }

    private void axeStrike(CombatModeContext context, LivingEntity target) {
        double distance = context.motion().distanceTo(target);
        if (distance > context.tuning().attackRange() || !context.actions().canAttack()) {
            guardedTicks = 0;
            transitionTo(Phase.GUARD);
            return;
        }
        lowerShield(context);
        context.actions().attack(target, BotInventoryController.SWORD_SLOT);
        guardedTicks = 0;
        shieldHoldTicks = 3;
        transitionTo(Phase.GUARD);
    }

    private void raiseShield(CombatModeContext context) {
        shieldRaised = context.actions().isDefendingWithOffhand();
        if (shieldRaised || shieldRearmTicks > 0) {
            return;
        }
        context.actions().defendWithOffhand();
        shieldRaised = context.actions().isDefendingWithOffhand();
    }

    private void lowerShield(CombatModeContext context) {
        if (!shieldRaised && !context.actions().isDefendingWithOffhand()) {
            return;
        }
        context.actions().releaseUseItem();
        shieldRaised = false;
        shieldRearmTicks = 1;
    }

    private void transitionTo(Phase nextPhase) {
        phase = nextPhase;
    }

    private enum Phase {
        GUARD,
        AXE_STRIKE
    }
}
