package com.monkey.ultimatebot.combat.mode.uhc;

import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.combat.mode.runtime.AbstractCombatModeStrategy;
import com.monkey.ultimatebot.combat.mode.runtime.CombatModeContext;
import com.monkey.ultimatebot.combat.mode.runtime.ModeKit;
import com.monkey.ultimatebot.combat.mode.shared.CobwebCombatAwareness;
import com.monkey.ultimatebot.combat.mode.shared.CombatBlockBreakSequence;
import com.monkey.ultimatebot.common.model.CombatMode;
import com.monkey.ultimatebot.compat.CombatCadenceAccess;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;

public final class UhcPvPStrategy extends AbstractCombatModeStrategy {
    private static final int AXE_SLOT = BotInventoryController.ENDERPEARL_SLOT;
    private static final int CROSSBOW_SLOT = BotInventoryController.TOTEM_SLOT;
    private static final int ARROW_SLOT = BotInventoryController.EMPTY_SLOT;

    private final UhcSecondaryController secondaryController = new UhcSecondaryController();
    private final UhcWebPressureController webPressureController = new UhcWebPressureController();
    private final CombatBlockBreakSequence blockBreakSequence = new CombatBlockBreakSequence();
    private Phase phase = Phase.PREGAP;
    private int phaseTicks;

    public UhcPvPStrategy() {
        super(
                CombatMode.UHC,
                ModeKit.builder()
                        .slot(BotInventoryController.SWORD_SLOT, Material.DIAMOND_SWORD)
                        .slot(AXE_SLOT, Material.DIAMOND_AXE)
                        .slot(CROSSBOW_SLOT, "CROSSBOW", Material.BOW, 1)
                        .slot(BotInventoryController.OBSIDIAN_SLOT, Material.WATER_BUCKET, 4)
                        .slot(BotInventoryController.CRYSTAL_SLOT, Material.LAVA_BUCKET, 2)
                        .slot(BotInventoryController.ANCHOR_SLOT, "COBWEB", Material.STRING, 16)
                        .slot(BotInventoryController.GOLDEN_APPLE_SLOT, Material.GOLDEN_APPLE, 64)
                        .slot(ARROW_SLOT, Material.ARROW, 64)
                        .offHand("SHIELD", Material.GOLDEN_APPLE)
                        .build());
    }

    @Override
    public void enter(CombatModeContext context) {
        super.enter(context);
        secondaryController.reset();
        webPressureController.reset(context);
        blockBreakSequence.reset(context);
        transitionTo(Phase.PREGAP);
    }

    @Override
    public void exit(CombatModeContext context) {
        secondaryController.reset();
        webPressureController.reset(context);
        blockBreakSequence.reset(context);
        super.exit(context);
    }

    @Override
    protected void execute(CombatModeContext context, LivingEntity target) {
        context.motion().aimAt(target);
        phaseTicks++;
        if (breakRestrainingWeb(context)) {
            webPressureController.reset(context);
            return;
        }
        CobwebCombatAwareness.Containment containment = CobwebCombatAwareness.inspect(target);
        if (containment.inside() && !containment.nearlyExiting()) {
            if (phase != Phase.WEB_PRESSURE) {
                secondaryController.reset();
                transitionTo(Phase.WEB_PRESSURE);
            }
            webPressureController.tick(context, target);
            return;
        }
        if (phase == Phase.WEB_PRESSURE) {
            webPressureController.reset(context);
            transitionTo(containment.nearlyExiting() && specialActionReady() ? Phase.SECONDARY : Phase.ENCOUNTER);
        } else if (containment.nearlyExiting() && phase != Phase.SECONDARY && specialActionReady()) {
            transitionTo(Phase.SECONDARY);
        }
                switch (phase) {
            case PREGAP:
                pregap(context);
                break;
            case ENCOUNTER:
                encounter(context, target);
                break;
            case SWORD_TRADE:
                swordTrade(context, target);
                break;
            case DISENGAGE:
                disengage(context, target);
                break;
            case CROSSBOW:
                crossbow(context, target);
                break;
            case SECONDARY:
                secondary(context, target);
                break;
            case HEAL:
                heal(context);
                break;
            case WEB_PRESSURE:
                webPressureController.tick(context, target);
                break;
        }
    }

    private void pregap(CombatModeContext context) {
        if (phaseTicks == 1) {
            context.actions().consumeGoldenApple(BotInventoryController.GOLDEN_APPLE_SLOT);
        }
        if (phaseTicks >= 5) {
            transitionTo(Phase.ENCOUNTER);
        }
    }

    private void encounter(CombatModeContext context, LivingEntity target) {
        context.actions().releaseUseItem();
        double distance = context.motion().distanceTo(target);
        if (distance > context.tuning().attackRange()) {
            context.motion().approach(target, 2.1D);
            return;
        }
        context.actions().attack(target, meleeSlot(context, target));
        transitionTo(Phase.SWORD_TRADE);
    }

    private void swordTrade(CombatModeContext context, LivingEntity target) {
        if (context.actions().healthRatio() <= 0.48D && specialActionReady()) {
            transitionTo(Phase.DISENGAGE);
            return;
        }
        if (phaseTicks >= 20 && specialActionReady()) {
            transitionTo(Phase.SECONDARY);
            return;
        }
        double distance = context.motion().distanceTo(target);
        if (distance <= context.tuning().attackRange()) {
            context.actions().releaseUseItem();
            context.actions().attack(target, meleeSlot(context, target));
        } else {
            context.motion().approach(target, 2.0D);
            if (distance <= 4.5D && phaseTicks % 9 == 0) {
                context.actions().defendWithOffhand();
            }
        }
    }

    private void disengage(CombatModeContext context, LivingEntity target) {
        context.actions().releaseUseItem();
        context.motion().retreat(target, 7.0D);
        if (context.motion().distanceTo(target) >= 5.5D || phaseTicks >= 10) {
            transitionTo(Phase.CROSSBOW);
        }
    }

    private void crossbow(CombatModeContext context, LivingEntity target) {
        if (phaseTicks == 1 && context.inventory().consumeItem(ARROW_SLOT)) {
            context.inventory().switchToSlot(CROSSBOW_SLOT);
            context.projectiles().fireArrow(target, context.tuning().aimAccuracy());
        }
        if (phaseTicks >= 3) {
            transitionTo(Phase.HEAL);
        }
    }

    private void secondary(CombatModeContext context, LivingEntity target) {
        if (phaseTicks == 1) {
            secondaryController.start(context, target);
            delaySpecialAction(context);
        }
        context.motion().retreat(target, 4.5D);
        if (secondaryController.tick(context, target, phaseTicks)) {
            secondaryController.reset();
            transitionTo(Phase.ENCOUNTER);
        }
    }

    private void heal(CombatModeContext context) {
        if (phaseTicks == 1 && context.actions().healthRatio() <= 0.58D) {
            context.actions().consumeGoldenApple(BotInventoryController.GOLDEN_APPLE_SLOT);
            delaySpecialAction(context);
        }
        if (phaseTicks >= 6) {
            transitionTo(Phase.ENCOUNTER);
        }
    }

    private boolean breakRestrainingWeb(CombatModeContext context) {
        java.util.List<org.bukkit.Location> occupiedWebs = CobwebCombatAwareness.occupiedWebs(context.bukkitBot());
        if (!occupiedWebs.isEmpty()) {
            return blockBreakSequence.tick(context, occupiedWebs.get(0), BotInventoryController.SWORD_SLOT);
        }
        blockBreakSequence.reset(context);
        return false;
    }

    private int meleeSlot(CombatModeContext context, LivingEntity target) {
        if (CombatCadenceAccess.hasWeaponCooldown() && context.actions().isTargetBlocking(target)) {
            return AXE_SLOT;
        }
        return BotInventoryController.SWORD_SLOT;
    }

    private void transitionTo(Phase nextPhase) {
        phase = nextPhase;
        phaseTicks = 0;
    }

    private enum Phase {
        PREGAP,
        ENCOUNTER,
        SWORD_TRADE,
        DISENGAGE,
        CROSSBOW,
        SECONDARY,
        HEAL,
        WEB_PRESSURE
    }
}
