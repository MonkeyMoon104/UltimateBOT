package com.monkey.ultimatebot.combat.mode.shared;


import java.util.Collections;
import java.util.List;
import com.monkey.ultimatebot.compat.EntityCoordsAccess;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public final class CobwebCombatAwareness {
    private CobwebCombatAwareness() {}

    public static Containment inspect(LivingEntity entity) {
        Location feet = new Location(
                entity.getWorld(),
                EntityCoordsAccess.getX(entity),
                EntityCoordsAccess.getY(entity) + 0.05D,
                EntityCoordsAccess.getZ(entity));
        Location head = new Location(
                entity.getWorld(),
                EntityCoordsAccess.getX(entity),
                EntityCoordsAccess.getY(entity) + entity.getHeight() * 0.75D,
                EntityCoordsAccess.getZ(entity));
        boolean feetWeb = isCobweb(feet);
        boolean headWeb = isCobweb(head);
        boolean inside = feetWeb || headWeb;
        return new Containment(inside, inside && nearlyExiting(entity.getLocation().toVector(), entity.getVelocity()));
    }

    public static List<Location> occupiedWebs(LivingEntity entity) {
        Location base = entity.getLocation().getBlock().getLocation();
        Location head = base.clone().add(0.0D, 1.0D, 0.0D);
        if (base.getBlock().getType() == Material.COBWEB && head.getBlock().getType() == Material.COBWEB) {
            return Collections.unmodifiableList(java.util.Arrays.asList(base, head));
        }
        if (base.getBlock().getType() == Material.COBWEB) {
            return Collections.unmodifiableList(java.util.Arrays.asList(base));
        }
        return head.getBlock().getType() == Material.COBWEB ? Collections.unmodifiableList(java.util.Arrays.asList(head)) : Collections.emptyList();
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

    public static final class Containment {
        private final boolean inside;
        private final boolean nearlyExiting;

        public Containment(boolean inside, boolean nearlyExiting) {
            this.inside = inside;
            this.nearlyExiting = nearlyExiting;
        }

        public boolean inside() {
            return inside;
        }
        public boolean nearlyExiting() {
            return nearlyExiting;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Containment)) {
                return false;
            }
            Containment other = (Containment) obj;
            return inside == other.inside && nearlyExiting == other.nearlyExiting;
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(inside, nearlyExiting);
        }

        @Override
        public String toString() {
            return "Containment[inside=" + inside + ", nearlyExiting=" + nearlyExiting + "]";
        }
    }
}
