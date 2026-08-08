package com.monkey.ultimatebot.combat.mode.shared;

import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public final class CobwebCombatAwareness {
    private CobwebCombatAwareness() {}

    public static Containment inspect(LivingEntity entity) {
        Location feet = new Location(entity.getWorld(), entity.getX(), entity.getY() + 0.05D, entity.getZ());
        Location head = new Location(entity.getWorld(), entity.getX(), entity.getY() + entity.getHeight() * 0.75D, entity.getZ());
        boolean feetWeb = isCobweb(feet);
        boolean headWeb = isCobweb(head);
        boolean inside = feetWeb || headWeb;
        return new Containment(inside, inside && nearlyExiting(entity.getLocation().toVector(), entity.getVelocity()));
    }

    public static List<Location> occupiedWebs(LivingEntity entity) {
        Location base = entity.getLocation().getBlock().getLocation();
        Location head = base.clone().add(0.0D, 1.0D, 0.0D);
        if (base.getBlock().getType() == Material.COBWEB && head.getBlock().getType() == Material.COBWEB) {
            return List.of(base, head);
        }
        if (base.getBlock().getType() == Material.COBWEB) {
            return List.of(base);
        }
        return head.getBlock().getType() == Material.COBWEB ? List.of(head) : List.of();
    }

    public static boolean nearlyExiting(Vector position, Vector velocity) {
        double horizontalSpeed = Math.hypot(velocity.getX(), velocity.getZ());
        if (horizontalSpeed < 0.003D) {
            return false;
        }
        double xFraction = position.getX() - Math.floor(position.getX());
        double zFraction = position.getZ() - Math.floor(position.getZ());
        double xExit = velocity.getX() > 0.0D ? 1.0D - xFraction : xFraction;
        double zExit = velocity.getZ() > 0.0D ? 1.0D - zFraction : zFraction;
        double nearestExit =
                Math.min(Math.abs(velocity.getX()) > 0.001D ? xExit : 1.0D, Math.abs(velocity.getZ()) > 0.001D ? zExit : 1.0D);
        return nearestExit <= 0.22D;
    }

    private static boolean isCobweb(Location position) {
        return position.getBlock().getType() == Material.COBWEB;
    }

    public record Containment(boolean inside, boolean nearlyExiting) {}
}
