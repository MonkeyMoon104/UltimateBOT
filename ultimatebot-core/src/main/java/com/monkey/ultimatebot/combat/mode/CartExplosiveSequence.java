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
    private static final int MAX_LIFETIME_TICKS = 80;

    private @Nullable UUID cartId;
    private @Nullable UUID arrowId;
    private @Nullable Location railLocation;
    private int lifetimeTicks;

    boolean placeRail(CombatModeContext context, LivingEntity target) {
        Location targetLocation =
                Objects.requireNonNull(target.getBukkitEntity().getLocation(), "target location");
        org.bukkit.util.Vector prediction =
                target.getBukkitEntity().getVelocity().clone().multiply(2.0D);
        Location placement = targetLocation.clone().add(prediction).getBlock().getLocation();
        if (!placement.clone().subtract(0.0D, 1.0D, 0.0D).getBlock().getType().isSolid()) {
            return false;
        }
        context.inventory().switchToSlot(RAIL_SLOT);
        if (!context.placeCombatBlock(placement, Material.RAIL, RAIL_SLOT)) {
            return false;
        }
        railLocation = placement;
        return true;
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
        return true;
    }

    boolean fireIgnitionArrow(CombatModeContext context, double accuracy) {
        Entity cart = cart();
        if (cart == null) {
            return false;
        }
        Arrow arrow = context.projectiles().fireIgnitionArrow(cart, accuracy);
        arrowId = arrow.getUniqueId();
        return true;
    }

    Status tick(CombatModeContext context) {
        Entity cart = cart();
        if (cart == null) {
            cleanup(context);
            return Status.FINISHED;
        }
        lifetimeTicks++;
        Entity arrow = arrowId == null ? null : Bukkit.getEntity(arrowId);
        if (arrow != null && arrow.isValid() && arrow.getWorld().equals(cart.getWorld())) {
            if (isArrowImpact(arrow.getLocation().distanceSquared(cart.getLocation()))) {
                detonate(context, cart);
                return Status.DETONATED;
            }
        }
        if (lifetimeTicks >= MAX_LIFETIME_TICKS) {
            cleanup(context);
            return Status.FINISHED;
        }
        return Status.ACTIVE;
    }

    boolean isActive() {
        return cart() != null;
    }

    static boolean isArrowImpact(double distanceSquared) {
        return Double.isFinite(distanceSquared) && distanceSquared <= 1.8D;
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

    private static void removeTrackedEntity(CombatModeContext context, @Nullable UUID entityId) {
        if (entityId != null) {
            context.entities().remove(entityId);
        }
    }

    enum Status {
        ACTIVE,
        DETONATED,
        FINISHED
    }
}
