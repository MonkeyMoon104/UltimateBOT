package com.monkey.ultimatebot.combat.mode;

import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.common.model.CombatMode;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;

final class AxeShieldPvPStrategy extends AbstractCombatModeStrategy {
    private Phase phase = Phase.GUARD;
    private int phaseTicks;

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
        transitionTo(Phase.GUARD);
    }

    @Override
    protected void execute(CombatModeContext context, LivingEntity target) {
        context.motion().aimAt(target);
        phaseTicks++;
        switch (phase) {
            case GUARD -> guard(context, target);
            case AXE_STRIKE -> axeStrike(context, target);
            case RECOVER -> recover(context, target);
        }
    }

    private void guard(CombatModeContext context, LivingEntity target) {
        context.inventory().switchToSlot(BotInventoryController.SWORD_SLOT);
        context.actions().defendWithOffhand();
        double distance = context.motion().distanceTo(target);
        if (distance > context.tuning().attackRange()) {
            context.motion().approach(target, 2.0D);
            return;
        }
        boolean safeStrike = !context.actions().isIncomingAttackLikely(target);
        if (context.actions().isTargetBlocking(target) || safeStrike || phaseTicks >= 12) {
            context.actions().releaseUseItem();
            transitionTo(Phase.AXE_STRIKE);
        }
    }

    private void axeStrike(CombatModeContext context, LivingEntity target) {
        context.actions().releaseUseItem();
        if (context.motion().distanceTo(target) > context.tuning().attackRange()) {
            context.motion().approach(target, 1.8D);
            return;
        }
        context.actions().attack(target, BotInventoryController.SWORD_SLOT);
        transitionTo(Phase.RECOVER);
    }

    private void recover(CombatModeContext context, LivingEntity target) {
        context.motion().retreat(target, 3.4D);
        if (phaseTicks >= Math.max(3, context.tuning().reactionTicks())) {
            transitionTo(Phase.GUARD);
        }
    }

    private void transitionTo(Phase nextPhase) {
        phase = nextPhase;
        phaseTicks = 0;
    }

    private enum Phase {
        GUARD,
        AXE_STRIKE,
        RECOVER
    }
}
