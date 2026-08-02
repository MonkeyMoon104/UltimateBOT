package com.monkey.ultimatebot.combat.mode;

import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.common.model.CombatMode;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;

final class WaterPvPStrategy extends AbstractCombatModeStrategy {
    private Phase phase = Phase.WAIT_FOR_WATER;
    private int phaseTicks;

    WaterPvPStrategy() {
        super(
                CombatMode.WATER,
                ModeKit.builder()
                        .slot(BotInventoryController.SWORD_SLOT, Items.DIAMOND_SWORD)
                        .slot(BotInventoryController.ENDERPEARL_SLOT, Items.WATER_BUCKET)
                        .slot(BotInventoryController.GOLDEN_APPLE_SLOT, Items.GOLDEN_APPLE, 64)
                        .diamondArmor()
                        .build());
    }

    @Override
    public void enter(CombatModeContext context) {
        super.enter(context);
        transitionTo(Phase.WAIT_FOR_WATER);
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
        double verticalSpeed = Math.clamp((target.getEyeY() - context.bot().getEyeY()) * 0.18D, -0.18D, 0.18D);
        context.motion().steerVelocityTowards(target, 0.34D, verticalSpeed);
    }

    private void swordTrade(CombatModeContext context, LivingEntity target) {
        double distance = context.motion().distanceTo(target);
        if (distance > context.tuning().attackRange() + 0.8D) {
            transitionTo(Phase.SWIM_APPROACH);
            return;
        }
        context.motion()
                .steerVelocityTowards(
                        target, 0.18D, Math.clamp((target.getY() - context.bot().getY()) * 0.12D, -0.12D, 0.12D));
        context.actions().attack(target, BotInventoryController.SWORD_SLOT);
        if (phaseTicks >= 12) {
            transitionTo(Phase.RECOVER);
        }
    }

    private void recover(CombatModeContext context, LivingEntity target) {
        context.motion().steerVelocityTowards(target, -0.23D, 0.04D);
        if (phaseTicks >= 6) {
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
