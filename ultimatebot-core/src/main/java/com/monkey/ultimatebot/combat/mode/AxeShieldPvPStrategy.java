package com.monkey.ultimatebot.combat.mode;

import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.common.model.CombatMode;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;

final class AxeShieldPvPStrategy extends AbstractCombatModeStrategy {
    private static final int AXE_SLOT = BotInventoryController.ENDERPEARL_SLOT;

    private Phase phase = Phase.APPROACH;
    private int phaseTicks;

    AxeShieldPvPStrategy() {
        super(
                CombatMode.AXE_SHIELD,
                ModeKit.builder()
                        .slot(BotInventoryController.SWORD_SLOT, Items.NETHERITE_SWORD)
                        .slot(AXE_SLOT, Items.NETHERITE_AXE)
                        .slot(BotInventoryController.GOLDEN_APPLE_SLOT, Items.GOLDEN_APPLE, 64)
                        .equipment(EquipmentSlot.OFFHAND, Items.SHIELD)
                        .netheriteArmor()
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
            case APPROACH -> approach(context, target);
            case DEFEND -> defend(context, target);
            case AXE_DISABLE -> axeDisable(context, target);
            case SWORD_FOLLOWUP -> swordFollowup(context, target);
            case RESET -> reset(context, target);
        }
    }

    private void approach(CombatModeContext context, LivingEntity target) {
        context.actions().releaseUseItem();
        double distance = context.motion().distanceTo(target);
        if (distance > context.tuning().attackRange()) {
            context.motion().approach(target, 2.15D);
            return;
        }
        if (context.actions().isTargetBlocking(target)) {
            transitionTo(Phase.AXE_DISABLE);
        } else {
            transitionTo(Phase.DEFEND);
        }
    }

    private void defend(CombatModeContext context, LivingEntity target) {
        context.inventory().switchToSlot(BotInventoryController.SWORD_SLOT);
        context.actions().defendWithOffhand();
        context.motion().strafe(target, 0.9D);
        if (context.actions().isTargetBlocking(target) || phaseTicks >= 5) {
            context.actions().releaseUseItem();
            transitionTo(context.actions().isTargetBlocking(target) ? Phase.AXE_DISABLE : Phase.SWORD_FOLLOWUP);
        }
    }

    private void axeDisable(CombatModeContext context, LivingEntity target) {
        context.actions().releaseUseItem();
        if (context.motion().distanceTo(target) > context.tuning().attackRange()) {
            context.motion().approach(target, 1.9D);
            return;
        }
        context.actions().attack(target, AXE_SLOT);
        transitionTo(Phase.SWORD_FOLLOWUP);
    }

    private void swordFollowup(CombatModeContext context, LivingEntity target) {
        if (context.motion().distanceTo(target) <= context.tuning().attackRange()) {
            context.actions().attack(target, BotInventoryController.SWORD_SLOT);
        } else {
            context.motion().approach(target, 1.75D);
        }
        if (phaseTicks >= 7) {
            transitionTo(Phase.RESET);
        }
    }

    private void reset(CombatModeContext context, LivingEntity target) {
        context.actions().defendWithOffhand();
        context.motion().retreat(target, 3.8D);
        if (phaseTicks >= 4) {
            context.actions().releaseUseItem();
            transitionTo(Phase.APPROACH);
        }
    }

    private void transitionTo(Phase nextPhase) {
        phase = nextPhase;
        phaseTicks = 0;
    }

    private enum Phase {
        APPROACH,
        DEFEND,
        AXE_DISABLE,
        SWORD_FOLLOWUP,
        RESET
    }
}
