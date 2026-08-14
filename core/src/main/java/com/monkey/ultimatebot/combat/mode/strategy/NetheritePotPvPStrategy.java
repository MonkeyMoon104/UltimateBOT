package com.monkey.ultimatebot.combat.mode.strategy;

import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.combat.mode.runtime.AbstractCombatModeStrategy;
import com.monkey.ultimatebot.combat.mode.runtime.CombatModeContext;
import com.monkey.ultimatebot.combat.mode.runtime.ModeKit;
import com.monkey.ultimatebot.combat.mode.shared.ModeCombatPolicy;
import com.monkey.ultimatebot.common.model.CombatMode;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;

public final class NetheritePotPvPStrategy extends AbstractCombatModeStrategy {
    private static final int FIRST_POTION_SLOT = BotInventoryController.ENDERPEARL_SLOT;
    private static final int SECOND_POTION_SLOT = BotInventoryController.TOTEM_SLOT;

    private Phase phase = Phase.PREBUFF;
    private int phaseTicks;
    private boolean potionPrepared;

    public NetheritePotPvPStrategy() {
        super(
                CombatMode.NETHERITE_POT,
                ModeKit.builder()
                        .slot(BotInventoryController.SWORD_SLOT, "NETHERITE_SWORD", Material.DIAMOND_SWORD, 1)
                        .slot(FIRST_POTION_SLOT, Material.SPLASH_POTION, 64)
                        .slot(SECOND_POTION_SLOT, Material.SPLASH_POTION, 64)
                        .slot(BotInventoryController.OBSIDIAN_SLOT, Material.ENDER_PEARL, 16)
                        .slot(BotInventoryController.GOLDEN_APPLE_SLOT, Material.GOLDEN_APPLE, 64)
                        .equipment(EquipmentSlot.OFF_HAND, Material.TOTEM_OF_UNDYING)
                        .build());
    }

    @Override
    public void enter(CombatModeContext context) {
        super.enter(context);
        potionPrepared = false;
        transitionTo(Phase.PREBUFF);
    }

    @Override
    protected void execute(CombatModeContext context, LivingEntity target) {
        context.motion().aimAt(target);
        phaseTicks++;
                switch (phase) {
            case PREBUFF:
                prebuff(context);
                break;
            case TRADE:
                trade(context, target);
                break;
            case CREATE_DISTANCE:
                createDistance(context, target);
                break;
            case FIRST_SPLASH:
                splash(context, FIRST_POTION_SLOT, Phase.SECOND_SPLASH);
                break;
            case SECOND_SPLASH:
                splash(context, SECOND_POTION_SLOT, Phase.REENTER);
                break;
            case EAT_GAPPLE:
                eatGapple(context);
                break;
            case REENTER:
                reenter(context, target);
                break;
        }
    }

    private void prebuff(CombatModeContext context) {
        if (phaseTicks == 1 && context.inventory().consumeItem(FIRST_POTION_SLOT)) {
            context.inventory().switchToSlot(FIRST_POTION_SLOT);
            potionPrepared = true;
        }
        if (phaseTicks == 2 && potionPrepared) {
            context.projectiles().throwSplashPotionDownward(Color.PURPLE);
        }
        if (phaseTicks == 4 && potionPrepared) {
            context.actions().applyCombatBuffs();
        }
        if (phaseTicks >= 6) {
            transitionTo(Phase.TRADE);
        }
    }

    private void trade(CombatModeContext context, LivingEntity target) {
        if (ModeCombatPolicy.shouldUsePotions(
                context.actions().healthRatio(), context.tuning().healingHealthRatio(), specialActionReady())) {
            transitionTo(Phase.CREATE_DISTANCE);
            return;
        }
        if (context.actions().healthRatio() <= 0.78D
                && context.random().nextDouble() < 0.025D
                && specialActionReady()) {
            transitionTo(Phase.EAT_GAPPLE);
            return;
        }
        double distance = context.motion().distanceTo(target);
        if (distance <= context.tuning().attackRange()) {
            context.actions().attack(target, BotInventoryController.SWORD_SLOT);
        }
        if (distance > 2.4D) {
            context.motion().approach(target, 1.8D);
        } else if (phaseTicks % 7 == 0) {
            context.motion().strafe(target, 1.45D);
        }
    }

    private void createDistance(CombatModeContext context, LivingEntity target) {
        if (context.motion().distanceTo(target) <= context.tuning().attackRange() && phaseTicks == 1) {
            context.actions().attack(target, BotInventoryController.SWORD_SLOT);
        }
        context.motion().retreat(target, 5.0D);
        if (context.motion().distanceTo(target) >= 4.0D || phaseTicks >= 8) {
            transitionTo(Phase.FIRST_SPLASH);
        }
    }

    private void splash(CombatModeContext context, int slot, Phase nextPhase) {
        if (phaseTicks == 1 && context.inventory().consumeItem(slot)) {
            context.inventory().switchToSlot(slot);
            potionPrepared = true;
        }
        if (phaseTicks == 2 && potionPrepared) {
            context.projectiles().throwSplashPotionDownward(Color.RED);
        }
        if (phaseTicks == 4 && potionPrepared) {
            context.actions().applyInstantHealth(1);
        }
        if (phaseTicks >= 5) {
            transitionTo(nextPhase);
        }
    }

    private void eatGapple(CombatModeContext context) {
        if (phaseTicks == 1) {
            context.actions().consumeGoldenApple(BotInventoryController.GOLDEN_APPLE_SLOT);
            delaySpecialAction(context);
        }
        if (phaseTicks >= 5) {
            transitionTo(Phase.REENTER);
        }
    }

    private void reenter(CombatModeContext context, LivingEntity target) {
        context.motion().approach(target, 1.8D);
        if (phaseTicks >= 7
                || context.motion().distanceTo(target) <= context.tuning().attackRange()) {
            delaySpecialAction(context);
            transitionTo(Phase.TRADE);
        }
    }

    private void transitionTo(Phase nextPhase) {
        phase = nextPhase;
        phaseTicks = 0;
        potionPrepared = false;
    }

    private enum Phase {
        PREBUFF,
        TRADE,
        CREATE_DISTANCE,
        FIRST_SPLASH,
        SECOND_SPLASH,
        EAT_GAPPLE,
        REENTER
    }
}
