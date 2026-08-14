package com.monkey.ultimatebot.combat.mode.runtime;

import com.monkey.ultimatebot.combat.mode.shared.ModeCombatPolicy;
import com.monkey.ultimatebot.compat.PlayerSwingAccess;
import java.util.Objects;
import java.util.SplittableRandom;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Trident;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.util.Vector;

public final class ModeProjectileService {
    private final org.bukkit.entity.Player shooter;
    private final ModeEntityTracker tracker;
    private final SplittableRandom random;

    ModeProjectileService(org.bukkit.entity.Player shooter, ModeEntityTracker tracker, SplittableRandom random) {
        this.shooter = Objects.requireNonNull(shooter, "shooter");
        this.tracker = Objects.requireNonNull(tracker, "tracker");
        this.random = Objects.requireNonNull(random, "random");
    }

    public Arrow fireArrow(LivingEntity target, double accuracy) {
        Arrow arrow = tracker.track(shooter.launchProjectile(Arrow.class, velocity(target, accuracy, 3.1D)));
        arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
        arrow.setCritical(accuracy >= 0.85D);
        return arrow;
    }

    public Arrow fireIgnitionArrow(org.bukkit.entity.Entity target, double accuracy, int drawTicks) {
        double speed = Math.max(0.75D, ModeCombatPolicy.bowPower(drawTicks) * 3.0D);
        Arrow arrow = tracker.track(
                shooter.launchProjectile(Arrow.class, ballisticVelocity(target, Math.max(0.96D, accuracy), speed)));
        arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
        arrow.setFireTicks(100);
        return arrow;
    }

    public void fireTrident(LivingEntity target, double accuracy) {
        Trident trident = tracker.track(shooter.launchProjectile(Trident.class, velocity(target, accuracy, 2.5D)));
        trident.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
    }

    public void fireWindCharge(LivingEntity target, double accuracy) {
        tracker.track(shooter.launchProjectile(windChargeType(), velocity(target, accuracy, 1.6D)));
    }

    public void fireEnderPearlAwayFrom(LivingEntity target) {
        Location shooterLocation = Objects.requireNonNull(shooter.getLocation(), "shooter location");
        Location targetLocation = Objects.requireNonNull(target.getLocation(), "target location");
        Vector away =
                shooterLocation.toVector().subtract(targetLocation.toVector()).setY(0.0D);
        if (away.lengthSquared() < 0.001D) {
            away = shooterLocation.getDirection().multiply(-1.0D).setY(0.0D);
        }
        tracker.track(shooter.launchProjectile(
                EnderPearl.class, away.normalize().multiply(1.45D).setY(0.48D)));
    }

    public void launchSelfWindCharge() {
        tracker.track(shooter.launchProjectile(windChargeType(), new Vector(0.0D, -1.35D, 0.0D)));
    }

    /**
     * Resolves {@code WindCharge} by name so this class can load on servers that predate 1.21.
     */
    @SuppressWarnings("unchecked")
    private static Class<? extends Projectile> windChargeType() {
        try {
            Class<?> type = Class.forName("org.bukkit.entity.WindCharge");
            if (!Projectile.class.isAssignableFrom(type)) {
                throw new IllegalStateException("WindCharge is not a Projectile on this server");
            }
            return (Class<? extends Projectile>) type;
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("WindCharge is unavailable on this server", exception);
        }
    }

    public void throwSplashPotionDownward(Color color) {
        Objects.requireNonNull(color, "color");
        ItemStack item = new ItemStack(Material.SPLASH_POTION);
        if (item.getItemMeta() instanceof PotionMeta) { PotionMeta potionMeta = (PotionMeta) item.getItemMeta();
            potionMeta.setColor(color);
            item.setItemMeta(potionMeta);
        }
        ThrownPotion potion =
                tracker.track(shooter.launchProjectile(ThrownPotion.class, new Vector(0.0D, -0.85D, 0.0D)));
        potion.setItem(item);
        PlayerSwingAccess.swingMainHand(shooter);
    }

    private Vector velocity(LivingEntity target, double accuracy, double speed) {
        return velocity(target, target.getHeight() * 0.65D, accuracy, speed);
    }

    private Vector velocity(org.bukkit.entity.Entity target, double heightOffset, double accuracy, double speed) {
        Location origin = shooter.getEyeLocation();
        Location destination = target.getLocation().add(0.0D, heightOffset, 0.0D);
        return velocity(origin, destination, accuracy, speed);
    }

    private Vector ballisticVelocity(org.bukkit.entity.Entity target, double accuracy, double speed) {
        Location origin = shooter.getEyeLocation();
        Location destination = target.getLocation().add(0.0D, target.getHeight() * 0.5D, 0.0D);
        double flightTicks = origin.distance(destination) / speed;
        destination.add(0.0D, Math.min(2.0D, 0.025D * flightTicks * flightTicks), 0.0D);
        return velocity(origin, destination, accuracy, speed);
    }

    private Vector velocity(Location origin, Location destination, double accuracy, double speed) {
        Vector direction = destination.toVector().subtract(origin.toVector()).normalize();
        double spread = ModeCombatPolicy.projectileSpread(accuracy);
        if (spread > 0.0D) {
            direction.add(new Vector(
                    random.nextDouble(-spread, spread),
                    random.nextDouble(-spread * 0.5D, spread * 0.5D),
                    random.nextDouble(-spread, spread)));
        }
        return direction.normalize().multiply(speed);
    }
}
