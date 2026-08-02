package com.monkey.ultimatebot.combat.mode;

import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.common.model.CombatMode;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.jspecify.annotations.Nullable;

final class CartPvPStrategy extends AbstractCombatModeStrategy {
    private static final int BOW_SLOT = BotInventoryController.ENDERPEARL_SLOT;
    private static final int RAIL_SLOT = BotInventoryController.OBSIDIAN_SLOT;
    private static final int CART_SLOT = BotInventoryController.CRYSTAL_SLOT;
    private static final int ARROW_SLOT = BotInventoryController.EMPTY_SLOT;

    private Phase phase = Phase.MELEE;
    private int phaseTicks;
    private @Nullable UUID activeCartId;
    private @Nullable Location railLocation;
    private int cartFuseTicks;

    CartPvPStrategy() {
        super(
                CombatMode.CART,
                ModeKit.builder()
                        .slot(BotInventoryController.SWORD_SLOT, Items.NETHERITE_SWORD)
                        .slot(BOW_SLOT, Items.BOW)
                        .slot(RAIL_SLOT, Items.RAIL, 64)
                        .slot(CART_SLOT, Items.TNT_MINECART, 64)
                        .slot(ARROW_SLOT, Items.ARROW, 64)
                        .netheriteArmor()
                        .build());
    }

    @Override
    public void enter(CombatModeContext context) {
        super.enter(context);
        activeCartId = null;
        railLocation = null;
        cartFuseTicks = 0;
        transitionTo(Phase.MELEE);
    }

    @Override
    protected void execute(CombatModeContext context, LivingEntity target) {
        context.motion().aimAt(target);
        phaseTicks++;
        if (activeCartId != null) {
            if (!isCartActive()) {
                activeCartId = null;
                restoreRail(context);
                transitionTo(Phase.RECOVER);
            } else if (--cartFuseTicks <= 0) {
                detonateCart(context);
                transitionTo(Phase.RECOVER);
            }
        }
        switch (phase) {
            case MELEE -> melee(context, target);
            case FIRE_ARROW -> fireArrow(context, target);
            case PLACE_RAIL -> placeRail(context, target);
            case PLACE_CART -> placeCart(context, target);
            case EVADE -> evade(context, target);
            case RECOVER -> recover(context, target);
        }
    }

    private void melee(CombatModeContext context, LivingEntity target) {
        double distance = context.motion().distanceTo(target);
        boolean opportunity = ModeCombatPolicy.isCartOpportunity(
                distance,
                context.bot().getY(),
                target.getY(),
                context.actions().targetHealthRatio(target),
                currentTick() % 60L == 0L);
        if (opportunity && specialActionReady()) {
            transitionTo(Phase.FIRE_ARROW);
            return;
        }
        meleeOrMove(context, target, BotInventoryController.SWORD_SLOT);
    }

    private void fireArrow(CombatModeContext context, LivingEntity target) {
        if (phaseTicks == 1 && context.inventory().consumeItem(ARROW_SLOT)) {
            context.inventory().switchToSlot(BOW_SLOT);
            context.projectiles()
                    .fireArrow(target, Math.min(1.0D, context.tuning().aimAccuracy() + 0.08D));
        }
        if (phaseTicks >= 2) {
            transitionTo(Phase.PLACE_RAIL);
        }
    }

    private void placeRail(CombatModeContext context, LivingEntity target) {
        Location targetLocation =
                Objects.requireNonNull(target.getBukkitEntity().getLocation(), "target location");
        Location placement = targetLocation.getBlock().getLocation();
        boolean supported = placement
                .clone()
                .subtract(0.0D, 1.0D, 0.0D)
                .getBlock()
                .getType()
                .isSolid();
        if (!supported
                || !context.inventory().consumeItem(RAIL_SLOT)
                || !context.placeTemporaryBlock(placement, Material.RAIL)) {
            delaySpecialAction(context);
            transitionTo(Phase.RECOVER);
            return;
        }
        railLocation = placement;
        transitionTo(Phase.PLACE_CART);
    }

    private void placeCart(CombatModeContext context, LivingEntity target) {
        Location placement = railLocation;
        if (placement == null || !context.inventory().consumeItem(CART_SLOT)) {
            restoreRail(context);
            transitionTo(Phase.RECOVER);
            return;
        }
        Location spawnLocation = placement.clone().add(0.5D, 0.1D, 0.5D);
        ExplosiveMinecart cart =
                context.entities().track(spawnLocation.getWorld().spawn(spawnLocation, ExplosiveMinecart.class));
        cart.setFuseTicks(-1);
        cart.setVelocity(target.getBukkitEntity().getVelocity().multiply(0.25D));
        activeCartId = cart.getUniqueId();
        cartFuseTicks = 14;
        delaySpecialAction(context);
        transitionTo(Phase.EVADE);
    }

    private void evade(CombatModeContext context, LivingEntity target) {
        context.motion().retreat(target, 7.0D);
        if (phaseTicks >= 22) {
            if (activeCartId != null) {
                context.entities().remove(activeCartId);
                activeCartId = null;
            }
            restoreRail(context);
            transitionTo(Phase.RECOVER);
        }
    }

    private void recover(CombatModeContext context, LivingEntity target) {
        context.motion().strafe(target, 1.8D);
        if (phaseTicks >= 8) {
            transitionTo(Phase.MELEE);
        }
    }

    private boolean isCartActive() {
        Entity entity = activeCartId == null ? null : org.bukkit.Bukkit.getEntity(activeCartId);
        return entity != null && entity.isValid();
    }

    private void detonateCart(CombatModeContext context) {
        UUID cartId = activeCartId;
        if (cartId == null) {
            return;
        }
        Entity entity = org.bukkit.Bukkit.getEntity(cartId);
        activeCartId = null;
        if (entity != null) {
            Location explosionLocation = Objects.requireNonNull(entity.getLocation(), "cart location");
            context.entities().remove(cartId);
            explosionLocation
                    .getWorld()
                    .createExplosion(
                            explosionLocation,
                            4.0F,
                            false,
                            context.options().isExplosionBlockDamage(),
                            context.bukkitBot());
        }
        restoreRail(context);
    }

    private void restoreRail(CombatModeContext context) {
        if (railLocation != null) {
            context.restoreTemporaryBlock(railLocation);
            railLocation = null;
        }
    }

    private void transitionTo(Phase nextPhase) {
        phase = nextPhase;
        phaseTicks = 0;
    }

    private enum Phase {
        MELEE,
        FIRE_ARROW,
        PLACE_RAIL,
        PLACE_CART,
        EVADE,
        RECOVER
    }
}
