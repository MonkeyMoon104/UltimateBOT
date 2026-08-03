package com.monkey.ultimatebot.combat.mode;

import java.util.Objects;
import java.util.random.RandomGenerator;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Trident;
import org.bukkit.entity.WindCharge;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.util.Vector;

final class ModeProjectileService {
    private final org.bukkit.entity.Player shooter;
    private final ModeEntityTracker tracker;
    private final RandomGenerator random;

    ModeProjectileService(org.bukkit.entity.Player shooter, ModeEntityTracker tracker, RandomGenerator random) {
        this.shooter = Objects.requireNonNull(shooter, "shooter");
        this.tracker = Objects.requireNonNull(tracker, "tracker");
        this.random = Objects.requireNonNull(random, "random");
    }

    Arrow fireArrow(LivingEntity target, double accuracy) {
        Arrow arrow = tracker.track(shooter.launchProjectile(Arrow.class, velocity(target, accuracy, 3.1D)));
        arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
        arrow.setCritical(accuracy >= 0.85D);
        return arrow;
    }

    Arrow fireIgnitionArrow(org.bukkit.entity.Entity target, double accuracy) {
        Arrow arrow = tracker.track(shooter.launchProjectile(Arrow.class, velocity(target, accuracy, 3.1D)));
        arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
        arrow.setFireTicks(100);
        return arrow;
    }

    void fireTrident(LivingEntity target, double accuracy) {
        Trident trident = tracker.track(shooter.launchProjectile(Trident.class, velocity(target, accuracy, 2.5D)));
        trident.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
    }

    void fireWindCharge(LivingEntity target, double accuracy) {
        tracker.track(shooter.launchProjectile(WindCharge.class, velocity(target, accuracy, 1.6D)));
    }

    void fireEnderPearlAwayFrom(LivingEntity target) {
        Location shooterLocation = Objects.requireNonNull(shooter.getLocation(), "shooter location");
        Location targetLocation =
                Objects.requireNonNull(target.getBukkitEntity().getLocation(), "target location");
        Vector away =
                shooterLocation.toVector().subtract(targetLocation.toVector()).setY(0.0D);
        if (away.lengthSquared() < 0.001D) {
            away = shooterLocation.getDirection().multiply(-1.0D).setY(0.0D);
        }
        tracker.track(shooter.launchProjectile(
                EnderPearl.class, away.normalize().multiply(1.45D).setY(0.48D)));
    }

    void launchSelfWindCharge() {
        tracker.track(shooter.launchProjectile(WindCharge.class, new Vector(0.0D, -1.35D, 0.0D)));
    }

    void throwSplashPotionDownward(Color color) {
        Objects.requireNonNull(color, "color");
        ItemStack item = new ItemStack(Material.SPLASH_POTION);
        if (item.getItemMeta() instanceof PotionMeta potionMeta) {
            potionMeta.setColor(color);
            item.setItemMeta(potionMeta);
        }
        ThrownPotion potion =
                tracker.track(shooter.launchProjectile(ThrownPotion.class, new Vector(0.0D, -0.85D, 0.0D)));
        potion.setItem(item);
        shooter.swingMainHand();
    }

    private Vector velocity(LivingEntity target, double accuracy, double speed) {
        org.bukkit.entity.Entity targetEntity = target.getBukkitEntity();
        return velocity(targetEntity, target.getBbHeight() * 0.65D, accuracy, speed);
    }

    private Vector velocity(org.bukkit.entity.Entity target, double accuracy, double speed) {
        return velocity(target, target.getHeight() * 0.5D, accuracy, speed);
    }

    private Vector velocity(org.bukkit.entity.Entity target, double heightOffset, double accuracy, double speed) {
        Location origin = shooter.getEyeLocation();
        Location destination = target.getLocation().add(0.0D, heightOffset, 0.0D);
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
