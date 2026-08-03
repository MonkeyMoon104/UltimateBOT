package com.monkey.ultimatebot.combat.mode;

import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.common.model.CombatMode;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;

final class AxeShieldPvPStrategy extends AbstractCombatModeStrategy {
    private Phase phase = Phase.GUARD;
    private int safeOpeningTicks;
    private int guardMovementCooldownTicks;
    private boolean counterStrike;

    AxeShieldPvPStrategy() {
        super(
                CombatMode.AXE_SHIELD,
                ModeKit.builder()
                        .slot(BotInventoryController.SWORD_SLOT, Items.NETHERITE_AXE)
                        .slot(BotInventoryController.GOLDEN_APPLE_SLOT, Items.GOLDEN_APPLE, 64)
                        .equipment(EquipmentSlot.OFFHAND, Items.SHIELD)
                        .build());
    }

    @Override
    public void enter(CombatModeContext context) {
        super.enter(context);
        safeOpeningTicks = 0;
        guardMovementCooldownTicks = 0;
        counterStrike = false;
        transitionTo(Phase.GUARD);
    }

    @Override
    protected void execute(CombatModeContext context, LivingEntity target) {
        context.actions().tickAttackCooldown();
        context.motion().aimAt(target);
        switch (phase) {
            case GUARD -> guard(context, target);
            case AXE_STRIKE -> axeStrike(context, target);
        }
    }

    private void guard(CombatModeContext context, LivingEntity target) {
        context.inventory().switchToSlot(BotInventoryController.SWORD_SLOT);
        context.actions().defendWithOffhand();
        double distance = context.motion().distanceTo(target);
        if (guardMovementCooldownTicks > 0) {
            guardMovementCooldownTicks--;
        }
        if (context.signals().consumeShieldImpact(target.getUUID())
                && ModeCombatPolicy.shouldCounterShieldImpact(
                        distance,
                        context.tuning().attackRange(),
                        context.actions().canAttack(),
                        context.tuning().aggression(),
                        context.random().nextDouble())) {
            counterStrike = true;
            transitionTo(Phase.AXE_STRIKE);
            return;
        }
        if (distance > context.tuning().attackRange()) {
            safeOpeningTicks = 0;
            context.motion().approach(target, 2.0D);
            return;
        }

        boolean incomingAttack = context.actions().isIncomingAttackLikely(target);
        if (incomingAttack) {
            safeOpeningTicks = 0;
            if (distance <= 1.9D && guardMovementCooldownTicks == 0) {
                context.motion().retreat(target, 3.2D);
                guardMovementCooldownTicks = 5;
            } else if (guardMovementCooldownTicks == 0) {
                context.motion().stop();
            }
            return;
        }

        safeOpeningTicks++;
        if (ModeCombatPolicy.isSafeAxeOpening(
                distance,
                context.tuning().attackRange(),
                false,
                context.actions().canAttack(),
                safeOpeningTicks)) {
            transitionTo(Phase.AXE_STRIKE);
        }
    }

    private void axeStrike(CombatModeContext context, LivingEntity target) {
        double distance = context.motion().distanceTo(target);
        if ((!counterStrike && context.actions().isIncomingAttackLikely(target))
                || distance > context.tuning().attackRange()
                || !context.actions().canAttack()) {
            safeOpeningTicks = 0;
            counterStrike = false;
            transitionTo(Phase.GUARD);
            return;
        }
        context.actions().releaseUseItem();
        context.actions().attack(target, BotInventoryController.SWORD_SLOT);
        safeOpeningTicks = 0;
        counterStrike = false;
        transitionTo(Phase.GUARD);
    }

    private void transitionTo(Phase nextPhase) {
        phase = nextPhase;
    }

    private enum Phase {
        GUARD,
        AXE_STRIKE
    }
}
