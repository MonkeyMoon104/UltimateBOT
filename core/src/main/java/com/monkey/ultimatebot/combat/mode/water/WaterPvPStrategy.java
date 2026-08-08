package com.monkey.ultimatebot.combat.mode.water;

import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.combat.mode.runtime.AbstractCombatModeStrategy;
import com.monkey.ultimatebot.combat.mode.runtime.CombatModeContext;
import com.monkey.ultimatebot.combat.mode.runtime.ModeKit;
import com.monkey.ultimatebot.combat.mode.shared.ModeCombatPolicy;
import com.monkey.ultimatebot.common.model.CombatMode;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;

public final class WaterPvPStrategy extends AbstractCombatModeStrategy {
    private Phase phase = Phase.WAIT_FOR_WATER;
    private int phaseTicks;
    private double strafeDirection;

    public WaterPvPStrategy() {
        super(
                CombatMode.WATER,
                ModeKit.builder()
                        .slot(BotInventoryController.SWORD_SLOT, Material.DIAMOND_SWORD)
                        .slot(BotInventoryController.ENDERPEARL_SLOT, Material.WATER_BUCKET)
                        .slot(BotInventoryController.GOLDEN_APPLE_SLOT, Material.GOLDEN_APPLE, 64)
                        .build());
    }

    @Override
    public void enter(CombatModeContext context) {
        super.enter(context);
        strafeDirection = context.random().nextBoolean() ? 1.0D : -1.0D;
        transitionTo(Phase.WAIT_FOR_WATER);
    }

    @Override
    public boolean controlsNavigation(CombatModeContext context, LivingEntity target) {
        return ModeCombatPolicy.canFightInWater(
                context.motion().isBotInWater(), context.motion().isTargetInWater(target));
    }

    @Override
    protected void execute(CombatModeContext context, LivingEntity target) {
        context.motion().aimAt(target);
        phaseTicks++;
        if (!ModeCombatPolicy.canFightInWater(
                context.motion().isBotInWater(), context.motion().isTargetInWater(target))) {
            context.motion().setSwimming(false);
            context.motion().stop();
            transitionTo(Phase.WAIT_FOR_WATER);
            return;
        }
        context.motion().setSwimming(true);
        switch (phase) {
            case WAIT_FOR_WATER -> transitionTo(Phase.SWIM_APPROACH);
            case SWIM_APPROACH -> swimApproach(context, target);
            case SWORD_TRADE -> swordTrade(context, target);
            case RECOVER -> recover(context, target);
        }
    }

    private void swimApproach(CombatModeContext context, LivingEntity target) {
        double distance = context.motion().distanceTo(target);
        if (distance <= context.tuning().attackRange()) {
            transitionTo(Phase.SWORD_TRADE);
            return;
        }
        context.motion().swimTowards(target, 0.36D);
    }

    private void swordTrade(CombatModeContext context, LivingEntity target) {
        double distance = context.motion().distanceTo(target);
        if (distance > context.tuning().attackRange() + 0.8D) {
            transitionTo(Phase.SWIM_APPROACH);
            return;
        }
        context.motion().swimOrbit(target, 0.06D, 0.18D, strafeDirection);
        context.actions().attackNormally(target, BotInventoryController.SWORD_SLOT);
        if (phaseTicks >= 12) {
            transitionTo(Phase.RECOVER);
        }
    }

    private void recover(CombatModeContext context, LivingEntity target) {
        context.motion().swimOrbit(target, -0.16D, 0.18D, strafeDirection);
        if (phaseTicks >= 6) {
            strafeDirection = context.random().nextBoolean() ? 1.0D : -1.0D;
            transitionTo(Phase.SWIM_APPROACH);
        }
    }

    private void transitionTo(Phase nextPhase) {
        phase = nextPhase;
        phaseTicks = 0;
    }

    private enum Phase {
        WAIT_FOR_WATER,
        SWIM_APPROACH,
        SWORD_TRADE,
        RECOVER
    }
}
