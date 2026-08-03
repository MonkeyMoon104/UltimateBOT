package com.monkey.ultimatebot.combat.mode;

import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.jspecify.annotations.Nullable;

final class CartExplosiveSequence {
    private static final int RAIL_SLOT = BotInventoryController.OBSIDIAN_SLOT;
    private static final int CART_SLOT = BotInventoryController.CRYSTAL_SLOT;
    private static final int MAX_LIFETIME_TICKS = 600;
    private static final int MAX_ARROW_FLIGHT_TICKS = 40;
    private static final double TARGET_BLAST_RANGE_SQUARED = 9.0D;

    private @Nullable UUID cartId;
    private @Nullable UUID arrowId;
    private @Nullable Location railLocation;
    private int lifetimeTicks;
    private int arrowFlightTicks;

    boolean placeRail(CombatModeContext context, LivingEntity target) {
        Location targetLocation =
                Objects.requireNonNull(target.getBukkitEntity().getLocation(), "target location");
        context.inventory().switchToSlot(RAIL_SLOT);
        for (Location placement : CartPlacementPlanner.railCandidates(
                targetLocation, target.getBukkitEntity().getVelocity())) {
            if (!placement
                    .clone()
                    .subtract(0.0D, 1.0D, 0.0D)
                    .getBlock()
                    .getType()
                    .isSolid()) {
                continue;
            }
            if (context.placeCombatBlock(placement, Material.RAIL, RAIL_SLOT)) {
                railLocation = placement;
                return true;
            }
        }
        return false;
    }

    boolean placeCart(CombatModeContext context) {
        Location rail = railLocation;
        if (rail == null || !context.inventory().consumeItem(CART_SLOT)) {
            return false;
        }
        context.inventory().switchToSlot(CART_SLOT);
        Location spawn = rail.clone().add(0.5D, 0.1D, 0.5D);
        ExplosiveMinecart cart = context.entities().track(spawn.getWorld().spawn(spawn, ExplosiveMinecart.class));
        cart.setFuseTicks(-1);
        cart.setInvulnerable(true);
        cart.setVelocity(new org.bukkit.util.Vector());
        cartId = cart.getUniqueId();
        lifetimeTicks = 0;
        arrowFlightTicks = 0;
        return true;
    }

    int requiredIgnitionDrawTicks(CombatModeContext context) {
        Entity cart = cart();
        if (cart == null) {
            return 20;
        }
        double distance = context.bukkitBot().getEyeLocation().distance(cart.getLocation());
        return ModeCombatPolicy.ignitionBowDrawTicks(distance);
    }

    boolean isTargetCloseForIgnition(LivingEntity target) {
        Entity cart = cart();
        return cart != null && targetDistanceSquared(cart, target) <= TARGET_BLAST_RANGE_SQUARED;
    }

    boolean fireIgnitionArrow(CombatModeContext context, double accuracy, int drawTicks) {
        Entity cart = cart();
        if (cart == null) {
            return false;
        }
        removeIgnitionArrow(context);
        Arrow arrow = context.projectiles().fireIgnitionArrow(cart, accuracy, drawTicks);
        arrowId = arrow.getUniqueId();
        arrowFlightTicks = 0;
        return true;
    }

    Status tick(CombatModeContext context, LivingEntity target) {
        Entity cart = cart();
        if (cart == null) {
            cleanup(context);
            return Status.FINISHED;
        }
        lifetimeTicks++;
        Entity arrow = arrowId == null ? null : Bukkit.getEntity(arrowId);
        if (arrowId != null) {
            arrowFlightTicks++;
            if (arrow == null || !arrow.isValid() || !arrow.getWorld().equals(cart.getWorld())) {
                removeIgnitionArrow(context);
                return Status.NEEDS_IGNITION;
            }
            if (isArrowImpact(arrow.getLocation().distanceSquared(cart.getLocation()))) {
                if (!isTargetInBlastRange(targetDistanceSquared(cart, target))) {
                    cleanup(context);
                    return Status.TARGET_ESCAPED;
                }
                detonate(context, cart);
                return Status.DETONATED;
            }
            if (arrowFlightTicks >= MAX_ARROW_FLIGHT_TICKS
                    || (arrowFlightTicks >= 5 && arrow.getVelocity().lengthSquared() < 0.0025D)) {
                removeIgnitionArrow(context);
                return Status.NEEDS_IGNITION;
            }
        }
        if (lifetimeTicks >= MAX_LIFETIME_TICKS) {
            cleanup(context);
            return Status.EXPIRED;
        }
        return Status.ACTIVE;
    }

    boolean isActive() {
        return cart() != null;
    }

    static boolean isArrowImpact(double distanceSquared) {
        return Double.isFinite(distanceSquared) && distanceSquared <= 1.8D;
    }

    static boolean isTargetInBlastRange(double distanceSquared) {
        return Double.isFinite(distanceSquared) && distanceSquared <= TARGET_BLAST_RANGE_SQUARED;
    }

    void cleanup(CombatModeContext context) {
        removeTrackedEntity(context, arrowId);
        removeTrackedEntity(context, cartId);
        arrowId = null;
        cartId = null;
        if (railLocation != null) {
            context.restoreCombatBlock(railLocation);
            railLocation = null;
        }
        lifetimeTicks = 0;
        arrowFlightTicks = 0;
    }

    private void detonate(CombatModeContext context, Entity cart) {
        Location explosion = cart.getLocation();
        cleanup(context);
        explosion
                .getWorld()
                .createExplosion(
                        explosion, 4.0F, false, context.options().canExplosionDamageBlocks(), context.bukkitBot());
    }

    private @Nullable Entity cart() {
        Entity cart = cartId == null ? null : Bukkit.getEntity(cartId);
        return cart != null && cart.isValid() ? cart : null;
    }

    private void removeIgnitionArrow(CombatModeContext context) {
        removeTrackedEntity(context, arrowId);
        arrowId = null;
        arrowFlightTicks = 0;
    }

    private static double targetDistanceSquared(Entity cart, LivingEntity target) {
        Entity bukkitTarget = target.getBukkitEntity();
        if (!cart.getWorld().equals(bukkitTarget.getWorld())) {
            return Double.POSITIVE_INFINITY;
        }
        return cart.getLocation().distanceSquared(bukkitTarget.getLocation());
    }

    private static void removeTrackedEntity(CombatModeContext context, @Nullable UUID entityId) {
        if (entityId != null) {
            context.entities().remove(entityId);
        }
    }

    enum Status {
        ACTIVE,
        NEEDS_IGNITION,
        TARGET_ESCAPED,
        DETONATED,
        EXPIRED,
        FINISHED
    }
}
