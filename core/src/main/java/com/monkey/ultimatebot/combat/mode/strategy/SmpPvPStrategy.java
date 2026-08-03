package com.monkey.ultimatebot.combat.mode.strategy;

import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.combat.mode.runtime.AbstractCombatModeStrategy;
import com.monkey.ultimatebot.combat.mode.runtime.CombatModeContext;
import com.monkey.ultimatebot.combat.mode.runtime.ModeKit;
import com.monkey.ultimatebot.common.model.CombatMode;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;

public final class SmpPvPStrategy extends AbstractCombatModeStrategy {
    private static final int AXE_SLOT = BotInventoryController.ENDERPEARL_SLOT;
    private static final int SHIELD_SLOT = BotInventoryController.TOTEM_SLOT;
    private static final int PEARL_SLOT = BotInventoryController.OBSIDIAN_SLOT;

    private Phase phase = Phase.PRESSURE;
    private int phaseTicks;
    private long nextShieldTick;

    public SmpPvPStrategy() {
        super(
                CombatMode.SMP,
                ModeKit.builder()
                        .slot(BotInventoryController.SWORD_SLOT, Items.NETHERITE_SWORD)
                        .slot(AXE_SLOT, Items.NETHERITE_AXE)
                        .slot(SHIELD_SLOT, Items.SHIELD)
                        .slot(PEARL_SLOT, Items.ENDER_PEARL, 16)
                        .slot(BotInventoryController.GOLDEN_APPLE_SLOT, Items.ENCHANTED_GOLDEN_APPLE, 64)
                        .equipment(EquipmentSlot.OFFHAND, Items.TOTEM_OF_UNDYING)
                        .build());
    }

    @Override
    public void enter(CombatModeContext context) {
        super.enter(context);
        nextShieldTick = 0L;
        transitionTo(Phase.PRESSURE);
    }

    @Override
    protected void execute(CombatModeContext context, LivingEntity target) {
        context.motion().aimAt(target);
        phaseTicks++;
        switch (phase) {
            case PRESSURE -> pressure(context, target);
            case SHIELD_DEFEND -> shieldDefend(context, target);
            case DISENGAGE -> disengage(context, target);
            case PEARL_ESCAPE -> pearlEscape(context, target);
            case HEAL -> heal(context);
            case REENTER -> reenter(context, target);
        }
    }

    private void pressure(CombatModeContext context, LivingEntity target) {
        context.actions().releaseUseItem();
        if (context.actions().healthRatio() <= 0.42D && specialActionReady()) {
            transitionTo(Phase.DISENGAGE);
            return;
        }
        if (currentTick() >= nextShieldTick && context.actions().isIncomingAttackLikely(target)) {
            transitionTo(Phase.SHIELD_DEFEND);
            return;
        }
        double distance = context.motion().distanceTo(target);
        if (distance <= context.tuning().attackRange()) {
            int weapon = context.actions().isTargetBlocking(target) ? AXE_SLOT : BotInventoryController.SWORD_SLOT;
            context.actions().attack(target, weapon);
        } else {
            context.motion().approach(target, 1.75D);
        }
    }

    private void shieldDefend(CombatModeContext context, LivingEntity target) {
        context.inventory().switchToSlot(SHIELD_SLOT);
        context.actions().useMainhandItem();
        if (context.motion().distanceTo(target) > 2.8D) {
            context.motion().approach(target, 2.3D);
        } else {
            context.motion().strafe(target, 0.75D);
        }
        boolean attackWindowOpened = phaseTicks >= 2 && !context.actions().isIncomingAttackLikely(target);
        if (attackWindowOpened || phaseTicks >= 12) {
            context.actions().releaseUseItem();
            nextShieldTick = currentTick() + Math.max(5L, context.tuning().reactionTicks());
            transitionTo(Phase.PRESSURE);
        }
    }

    private void disengage(CombatModeContext context, LivingEntity target) {
        context.actions().releaseUseItem();
        context.motion().retreat(target, 8.0D);
        if (context.motion().distanceTo(target) >= 5.5D || phaseTicks >= 8) {
            transitionTo(Phase.PEARL_ESCAPE);
        }
    }

    private void pearlEscape(CombatModeContext context, LivingEntity target) {
        if (phaseTicks == 1 && context.inventory().consumeItem(PEARL_SLOT)) {
            context.inventory().switchToSlot(PEARL_SLOT);
            context.projectiles().fireEnderPearlAwayFrom(target);
        }
        if (phaseTicks >= 3) {
            transitionTo(Phase.HEAL);
        }
    }

    private void heal(CombatModeContext context) {
        if (phaseTicks == 1) {
            context.actions().consumeGoldenApple(BotInventoryController.GOLDEN_APPLE_SLOT);
            delaySpecialAction(context);
        }
        if (phaseTicks >= 7) {
            transitionTo(Phase.REENTER);
        }
    }

    private void reenter(CombatModeContext context, LivingEntity target) {
        context.motion().approach(target, 1.75D);
        if (context.motion().distanceTo(target) <= context.tuning().attackRange() || phaseTicks >= 12) {
            transitionTo(Phase.PRESSURE);
        }
    }

    private void transitionTo(Phase nextPhase) {
        phase = nextPhase;
        phaseTicks = 0;
    }

    private enum Phase {
        PRESSURE,
        SHIELD_DEFEND,
        DISENGAGE,
        PEARL_ESCAPE,
        HEAL,
        REENTER
    }
}
