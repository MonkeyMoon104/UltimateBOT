package com.monkey.ultimatebot.combat.mode;

import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.common.model.CombatMode;
import java.util.Objects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import org.bukkit.Location;
import org.bukkit.Material;
import org.jspecify.annotations.Nullable;

final class UhcPvPStrategy extends AbstractCombatModeStrategy {
    private static final int AXE_SLOT = BotInventoryController.ENDERPEARL_SLOT;
    private static final int CROSSBOW_SLOT = BotInventoryController.TOTEM_SLOT;
    private static final int WATER_SLOT = BotInventoryController.OBSIDIAN_SLOT;
    private static final int LAVA_SLOT = BotInventoryController.CRYSTAL_SLOT;
    private static final int WEB_SLOT = BotInventoryController.ANCHOR_SLOT;
    private static final int ARROW_SLOT = BotInventoryController.EMPTY_SLOT;

    private Phase phase = Phase.PREGAP;
    private int phaseTicks;
    private @Nullable Location secondaryLocation;

    UhcPvPStrategy() {
        super(
                CombatMode.UHC,
                ModeKit.builder()
                        .slot(BotInventoryController.SWORD_SLOT, Items.DIAMOND_SWORD)
                        .slot(AXE_SLOT, Items.DIAMOND_AXE)
                        .slot(CROSSBOW_SLOT, Items.CROSSBOW)
                        .slot(WATER_SLOT, Items.WATER_BUCKET, 4)
                        .slot(LAVA_SLOT, Items.LAVA_BUCKET, 2)
                        .slot(WEB_SLOT, Items.COBWEB, 16)
                        .slot(BotInventoryController.GOLDEN_APPLE_SLOT, Items.GOLDEN_APPLE, 64)
                        .slot(ARROW_SLOT, Items.ARROW, 64)
                        .equipment(EquipmentSlot.OFFHAND, Items.SHIELD)
                        .diamondArmor()
                        .build());
    }

    @Override
    public void enter(CombatModeContext context) {
        super.enter(context);
        secondaryLocation = null;
        transitionTo(Phase.PREGAP);
    }

    @Override
    protected void execute(CombatModeContext context, LivingEntity target) {
        context.motion().aimAt(target);
        phaseTicks++;
        switch (phase) {
            case PREGAP -> pregap(context);
            case ENCOUNTER -> encounter(context, target);
            case SWORD_TRADE -> swordTrade(context, target);
            case DISENGAGE -> disengage(context, target);
            case CROSSBOW -> crossbow(context, target);
            case SECONDARY -> secondary(context, target);
            case HEAL -> heal(context);
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
        context.actions().attack(target, AXE_SLOT);
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
            context.actions()
                    .attack(
                            target,
                            context.actions().isTargetBlocking(target) ? AXE_SLOT : BotInventoryController.SWORD_SLOT);
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
            Location targetLocation = Objects.requireNonNull(
                            target.getBukkitEntity().getLocation(), "target location")
                    .getBlock()
                    .getLocation();
            if (context.random().nextBoolean()) {
                if (context.inventory().consumeItem(WEB_SLOT)
                        && context.placeTemporaryBlock(targetLocation, Material.COBWEB)) {
                    secondaryLocation = targetLocation;
                    if (target.getBukkitEntity() instanceof org.bukkit.entity.LivingEntity livingTarget) {
                        livingTarget.addPotionEffect(new org.bukkit.potion.PotionEffect(
                                org.bukkit.potion.PotionEffectType.SLOWNESS, 30, 3, false, false, false));
                    }
                }
            } else if (context.inventory().consumeItem(LAVA_SLOT)) {
                target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), 80));
                target.getBukkitEntity()
                        .getWorld()
                        .spawnParticle(
                                org.bukkit.Particle.LAVA,
                                Objects.requireNonNull(target.getBukkitEntity().getLocation(), "target location"),
                                12,
                                0.35D,
                                0.15D,
                                0.35D,
                                0.02D);
            }
            delaySpecialAction(context);
        }
        context.motion().retreat(target, 4.5D);
        if (phaseTicks >= 10) {
            if (secondaryLocation != null) {
                context.restoreTemporaryBlock(secondaryLocation);
                secondaryLocation = null;
            }
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
        HEAL
    }
}
