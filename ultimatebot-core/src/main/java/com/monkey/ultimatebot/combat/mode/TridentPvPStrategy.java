package com.monkey.ultimatebot.combat.mode;

import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.common.model.CombatMode;
import java.util.Objects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import org.bukkit.Location;
import org.bukkit.Material;
import org.jspecify.annotations.Nullable;

final class TridentPvPStrategy extends AbstractCombatModeStrategy {
    private static final int HOE_SLOT = BotInventoryController.ENDERPEARL_SLOT;
    private static final int WATER_SLOT = BotInventoryController.TOTEM_SLOT;
    private static final int WEB_SLOT = BotInventoryController.OBSIDIAN_SLOT;
    private static final int SPONGE_SLOT = BotInventoryController.CRYSTAL_SLOT;

    private Phase phase = Phase.PREPARE_WATER;
    private int phaseTicks;
    private @Nullable Location trapLocation;
    private @Nullable Location spongeLocation;

    TridentPvPStrategy() {
        super(
                CombatMode.TRIDENT,
                ModeKit.builder()
                        .slot(BotInventoryController.SWORD_SLOT, Items.TRIDENT)
                        .slot(HOE_SLOT, Items.NETHERITE_HOE)
                        .slot(WATER_SLOT, Items.WATER_BUCKET, 4)
                        .slot(WEB_SLOT, Items.COBWEB, 16)
                        .slot(SPONGE_SLOT, Items.SPONGE, 16)
                        .slot(BotInventoryController.GOLDEN_APPLE_SLOT, Items.GOLDEN_APPLE, 64)
                        .diamondArmor()
                        .build());
    }

    @Override
    public void enter(CombatModeContext context) {
        super.enter(context);
        trapLocation = null;
        spongeLocation = null;
        transitionTo(Phase.PREPARE_WATER);
    }

    @Override
    protected void execute(CombatModeContext context, LivingEntity target) {
        context.motion().aimAt(target);
        phaseTicks++;
        switch (phase) {
            case PREPARE_WATER -> prepareWater(context);
            case RIPTIDE -> riptide(context, target);
            case AIR_HIT -> airHit(context, target);
            case LOYALTY_THROW -> loyaltyThrow(context, target);
            case HOE_SWAP -> hoeSwap(context, target);
            case SPONGE_TRAP -> spongeTrap(context, target);
            case RECOVER -> recover(context, target);
        }
    }

    private void prepareWater(CombatModeContext context) {
        if (context.motion().isBotInWater()) {
            context.motion().setSwimming(true);
            transitionTo(Phase.RIPTIDE);
            return;
        }
        if (phaseTicks == 1 && context.inventory().consumeItem(WATER_SLOT)) {
            Location placement = Objects.requireNonNull(context.bukkitBot().getLocation(), "bot location")
                    .getBlock()
                    .getLocation();
            context.placeTemporaryBlock(placement, Material.WATER);
        }
        if (phaseTicks >= 4) {
            transitionTo(context.motion().isBotInWater() ? Phase.RIPTIDE : Phase.LOYALTY_THROW);
        }
    }

    private void riptide(CombatModeContext context, LivingEntity target) {
        context.inventory().switchToSlot(BotInventoryController.SWORD_SLOT);
        context.motion().setSwimming(context.motion().isBotInWater());
        context.motion()
                .propelTowards(
                        target,
                        0.72D,
                        Math.clamp((target.getY() - context.bot().getY()) * 0.22D + 0.24D, 0.12D, 0.52D));
        if (context.motion().distanceTo(target) <= 3.1D || phaseTicks >= 9) {
            transitionTo(Phase.AIR_HIT);
        }
    }

    private void airHit(CombatModeContext context, LivingEntity target) {
        context.motion().steerVelocityTowards(target, 0.38D, context.bot().getDeltaMovement().y);
        if (context.motion().distanceTo(target) <= context.tuning().attackRange()) {
            context.actions().attack(target, BotInventoryController.SWORD_SLOT);
            transitionTo(Phase.HOE_SWAP);
        } else if (phaseTicks >= 7) {
            transitionTo(Phase.LOYALTY_THROW);
        }
    }

    private void loyaltyThrow(CombatModeContext context, LivingEntity target) {
        double distance = context.motion().distanceTo(target);
        if (distance >= 4.0D && distance <= 28.0D && phaseTicks == 1) {
            context.inventory().switchToSlot(BotInventoryController.SWORD_SLOT);
            context.projectiles().fireTrident(target, context.tuning().aimAccuracy());
            delaySpecialAction(context);
        } else if (distance < 4.0D) {
            transitionTo(Phase.HOE_SWAP);
            return;
        }
        context.motion().strafe(target, 1.8D);
        if (phaseTicks >= 7) {
            transitionTo(Phase.SPONGE_TRAP);
        }
    }

    private void hoeSwap(CombatModeContext context, LivingEntity target) {
        if (context.motion().distanceTo(target) <= context.tuning().attackRange()) {
            context.actions().attack(target, HOE_SLOT);
            Location targetLocation =
                    Objects.requireNonNull(target.getBukkitEntity().getLocation(), "target location");
            Location botLocation = Objects.requireNonNull(context.bukkitBot().getLocation(), "bot location");
            org.bukkit.util.Vector knockback = targetLocation
                    .toVector()
                    .subtract(botLocation.toVector())
                    .normalize()
                    .multiply(0.45D)
                    .setY(0.16D);
            target.getBukkitEntity().setVelocity(knockback);
        }
        transitionTo(Phase.RECOVER);
    }

    private void spongeTrap(CombatModeContext context, LivingEntity target) {
        if (phaseTicks == 1 && context.motion().distanceTo(target) <= 5.0D) {
            Location targetBlock = Objects.requireNonNull(
                            target.getBukkitEntity().getLocation(), "target location")
                    .getBlock()
                    .getLocation();
            if (context.inventory().consumeItem(WEB_SLOT)
                    && context.placeTemporaryBlock(targetBlock, Material.COBWEB)) {
                trapLocation = targetBlock;
                if (target.getBukkitEntity() instanceof org.bukkit.entity.LivingEntity livingTarget) {
                    livingTarget.addPotionEffect(new org.bukkit.potion.PotionEffect(
                            org.bukkit.potion.PotionEffectType.SLOWNESS, 25, 3, false, false, false));
                }
            }
            Location adjacent = targetBlock.clone().add(1.0D, 0.0D, 0.0D);
            if (context.inventory().consumeItem(SPONGE_SLOT)
                    && context.placeTemporaryBlock(adjacent, Material.SPONGE)) {
                spongeLocation = adjacent;
            }
        }
        context.motion().retreat(target, 5.0D);
        if (phaseTicks >= 9) {
            restoreTrap(context);
            transitionTo(Phase.RECOVER);
        }
    }

    private void recover(CombatModeContext context, LivingEntity target) {
        context.motion().strafe(target, 1.5D);
        if (phaseTicks >= 6) {
            transitionTo(context.motion().isBotInWater() ? Phase.RIPTIDE : Phase.PREPARE_WATER);
        }
    }

    private void restoreTrap(CombatModeContext context) {
        if (trapLocation != null) {
            context.restoreTemporaryBlock(trapLocation);
            trapLocation = null;
        }
        if (spongeLocation != null) {
            context.restoreTemporaryBlock(spongeLocation);
            spongeLocation = null;
        }
    }

    private void transitionTo(Phase nextPhase) {
        phase = nextPhase;
        phaseTicks = 0;
    }

    private enum Phase {
        PREPARE_WATER,
        RIPTIDE,
        AIR_HIT,
        LOYALTY_THROW,
        HOE_SWAP,
        SPONGE_TRAP,
        RECOVER
    }
}
