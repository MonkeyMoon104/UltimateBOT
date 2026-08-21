package com.monkey.ultimatebot.combat.mode.strategy;

import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.combat.mode.runtime.AbstractCombatModeStrategy;
import com.monkey.ultimatebot.combat.mode.runtime.CombatModeContext;
import com.monkey.ultimatebot.combat.mode.runtime.ModeKit;
import com.monkey.ultimatebot.common.model.combat.CombatMode;
import com.monkey.ultimatebot.access.combat.CombatCadenceAccess;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;

public final class SwordPvPStrategy extends AbstractCombatModeStrategy {
    private Phase phase = Phase.SPACING;
    private int phaseTicks;

    public SwordPvPStrategy() {
        super(
                CombatMode.SWORD,
                ModeKit.builder()
                        .slot(BotInventoryController.SWORD_SLOT, Material.DIAMOND_SWORD)
                        .slot(BotInventoryController.ENDERPEARL_SLOT, Material.ENDER_PEARL, 16)
                        .slot(BotInventoryController.GOLDEN_APPLE_SLOT, Material.GOLDEN_APPLE, 64)
                        .build());
    }

    @Override
    public void enter(CombatModeContext context) {
        super.enter(context);
        transitionTo(Phase.SPACING);
    }

    @Override
    protected void execute(CombatModeContext context, LivingEntity target) {
        context.motion().aimAt(target);
        phaseTicks++;
        switch (phase) {
            case SPACING:
                spacing(context, target);
                break;
            case INITIATE:
                initiate(context, target);
                break;
            case CRIT_TRADE:
                critTrade(context, target);
                break;
            case COMBO:
                combo(context, target);
                break;
            case DEFLECT:
                deflect(context, target);
                break;
            case RESET:
                reset(context, target);
                break;
        }
    }

    private void spacing(CombatModeContext context, LivingEntity target) {
        if (context.actions().healthRatio() <= context.tuning().retreatHealthRatio()) {
            transitionTo(Phase.DEFLECT);
            return;
        }
        double distance = context.motion().distanceTo(target);
        if (distance > 3.15D) {
            context.motion().approach(target, 2.75D);
        } else if (distance < 2.25D) {
            context.motion().retreat(target, 3.0D);
        } else {
            transitionTo(Phase.INITIATE);
        }
    }

    private void initiate(CombatModeContext context, LivingEntity target) {
        if (!CombatCadenceAccess.hasWeaponCooldown()) {
            context.motion().approach(target, 2.0D);
            if (context.motion().distanceTo(target) <= context.tuning().attackRange()) {
                context.actions().attack(target, BotInventoryController.SWORD_SLOT);
                transitionTo(Phase.COMBO);
            } else if (phaseTicks >= 8) {
                transitionTo(Phase.SPACING);
            }
            return;
        }
        if (context.motion().isBotOnGround()) {
            context.motion().propelTowards(target, 0.34D, 0.34D);
        } else {
            context.motion()
                    .steerVelocityTowards(target, 0.31D, context.motion().botVerticalVelocity());
        }
        if (context.motion().distanceTo(target) <= context.tuning().attackRange()) {
            context.actions().attack(target, BotInventoryController.SWORD_SLOT);
            transitionTo(context.motion().botVerticalVelocity() < 0.0D ? Phase.CRIT_TRADE : Phase.COMBO);
        } else if (phaseTicks >= 8) {
            transitionTo(Phase.SPACING);
        }
    }

    private void critTrade(CombatModeContext context, LivingEntity target) {
        context.motion().steerVelocityTowards(target, 0.24D, context.motion().botVerticalVelocity());
        if (context.motion().distanceTo(target) <= context.tuning().attackRange()) {
            context.actions().attack(target, BotInventoryController.SWORD_SLOT);
        }
        if (context.motion().isBotOnGround() || phaseTicks >= 6) {
            transitionTo(Phase.COMBO);
        }
    }

    private void combo(CombatModeContext context, LivingEntity target) {
        context.motion()
                .steerVelocityTowards(
                        target,
                        0.29D,
                        context.motion().isBotOnGround()
                                ? 0.0D
                                : context.motion().botVerticalVelocity());
        if (context.motion().distanceTo(target) <= context.tuning().attackRange()) {
            context.actions().attack(target, BotInventoryController.SWORD_SLOT);
        }
        if (context.movement().hasRecentDamage() || phaseTicks >= 13) {
            transitionTo(Phase.DEFLECT);
        }
    }

    private void deflect(CombatModeContext context, LivingEntity target) {
        context.motion().retreat(target, 4.6D);
        if (phaseTicks == 3 && context.motion().isBotOnGround()) {
            context.motion().propelTowards(target, -0.25D, 0.28D);
        }
        if (phaseTicks >= 7) {
            transitionTo(Phase.RESET);
        }
    }

    private void reset(CombatModeContext context, LivingEntity target) {
        context.motion().strafe(target, 1.25D);
        if (phaseTicks >= 5) {
            transitionTo(Phase.SPACING);
        }
    }

    private void transitionTo(Phase nextPhase) {
        phase = nextPhase;
        phaseTicks = 0;
    }

    private enum Phase {
        SPACING,
        INITIATE,
        CRIT_TRADE,
        COMBO,
        DEFLECT,
        RESET
    }
}
