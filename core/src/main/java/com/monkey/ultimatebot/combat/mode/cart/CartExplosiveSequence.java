package com.monkey.ultimatebot.combat.mode.cart;

import com.monkey.ultimatebot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.ultimatebot.combat.mode.runtime.CombatModeContext;
import com.monkey.ultimatebot.combat.mode.shared.ModeCombatPolicy;
import com.monkey.ultimatebot.compat.ExplosiveMinecartAccess;
import com.monkey.ultimatebot.compat.MinecraftVersionAccess;
import com.monkey.ultimatebot.compat.WorldAccess;
import com.monkey.ultimatebot.nms.NMSBridgeManager;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.jspecify.annotations.Nullable;

final class CartExplosiveSequence {
    private static final int RAIL_SLOT = BotInventoryController.OBSIDIAN_SLOT;
    private static final int CART_SLOT = BotInventoryController.CRYSTAL_SLOT;
    private static final int PROXIMITY_FUSE_TICKS = 4;
    private static final int MAX_ARROW_FLIGHT_TICKS = 40;
    private static final double TARGET_BLAST_RANGE_SQUARED = 9.0D;

    private @Nullable UUID cartId;
    private @Nullable UUID arrowId;
    private @Nullable Location railLocation;
    private int lifetimeTicks;
    private int arrowFlightTicks;

    boolean placeRail(CombatModeContext context, LivingEntity target) {
        Location targetLocation = Objects.requireNonNull(target.getLocation(), "target location");
        context.inventory().switchToSlot(RAIL_SLOT);
        for (Location placement : CartPlacementPlanner.railCandidates(
                targetLocation, target.getVelocity())) {
            if (!placement
                    .clone()
                    .subtract(0.0D, 1.0D, 0.0D)
                    .getBlock()
                    .getType()
                    .isSolid()) {
                continue;
            }
            if (containsExplosiveCart(placement)) {
                continue;
            }
            if (placement.getBlock().getType() == Material.RAIL) {
                railLocation = placement;
                return true;
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
        ExplosiveMinecart cart = spawnCart(spawn);
        if (cart == null) {
            return false;
        }
        ExplosiveMinecartAccess.setFuseTicks(cart, -1);
        cart.setInvulnerable(false);
        cart.setVelocity(new org.bukkit.util.Vector());
        if (!context.trackCombatEntity(cart, CART_SLOT)) {
            return false;
        }
        cartId = cart.getUniqueId();
        lifetimeTicks = 0;
        arrowFlightTicks = 0;
        return true;
    }

    /** 1.17+: same Bukkit spawn as before. 1.16: NMS bridge only. */
    private static @Nullable ExplosiveMinecart spawnCart(Location spawn) {
        if (MinecraftVersionAccess.isAtLeast(1, 17)) {
            return spawn.getWorld().spawn(spawn, ExplosiveMinecart.class);
        }
        Entity spawned = NMSBridgeManager.get().spawnExplosiveMinecart(spawn);
        return spawned instanceof ExplosiveMinecart ? (ExplosiveMinecart) spawned : null;
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
            release(context);
            return Status.FINISHED;
        }
        lifetimeTicks++;
        if (shouldArmProximityFuse(lifetimeTicks, targetDistanceSquared(cart, target))) {
            armImmediateFuse(context, cart);
            return Status.DETONATED;
        }
        Entity arrow = arrowId == null ? null : Bukkit.getEntity(arrowId);
        if (arrowId != null) {
            arrowFlightTicks++;
            if (arrow == null || !arrow.isValid() || !arrow.getWorld().equals(cart.getWorld())) {
                removeIgnitionArrow(context);
                return Status.NEEDS_IGNITION;
            }
            if (isArrowImpact(arrow.getLocation().distanceSquared(cart.getLocation()))) {
                if (!isTargetInBlastRange(targetDistanceSquared(cart, target))) {
                    release(context);
                    return Status.TARGET_ESCAPED;
                }
                armImmediateFuse(context, cart);
                return Status.DETONATED;
            }
            if (arrowFlightTicks >= MAX_ARROW_FLIGHT_TICKS
                    || (arrowFlightTicks >= 5 && arrow.getVelocity().lengthSquared() < 0.0025D)) {
                removeIgnitionArrow(context);
                return Status.NEEDS_IGNITION;
            }
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

    static boolean shouldArmProximityFuse(int activeTicks, double targetDistanceSquared) {
        return activeTicks >= PROXIMITY_FUSE_TICKS && isTargetInBlastRange(targetDistanceSquared);
    }

    void release(CombatModeContext context) {
        removeTrackedEntity(context, arrowId);
        arrowId = null;
        cartId = null;
        railLocation = null;
        lifetimeTicks = 0;
        arrowFlightTicks = 0;
    }

    private void armImmediateFuse(CombatModeContext context, Entity cart) {
        if (cart instanceof ExplosiveMinecart) {
            ExplosiveMinecartAccess.setFuseTicks((ExplosiveMinecart) cart, 1);
        }
        release(context);
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
        if (!cart.getWorld().equals(target.getWorld())) {
            return Double.POSITIVE_INFINITY;
        }
        return cart.getLocation().distanceSquared(target.getLocation());
    }

    private static boolean containsExplosiveCart(Location rail) {
        Location center = rail.clone().add(0.5D, 0.25D, 0.5D);
        return !WorldAccess.nearbyEntitiesOfType(
                        center.getWorld(), center, 0.45D, 0.75D, 0.45D, ExplosiveMinecart.class)
                .isEmpty();
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
        FINISHED
    }
}
