package com.monkey.ultimatebot.combat.mode.shared;


import java.util.Collections;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.SplittableRandom;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public final class WebTrapPlanner {
    private WebTrapPlanner() {}

    public static List<Location> plan(
            Location targetLocation,
            Vector targetVelocity,
            boolean onGround,
            Vector lookDirection,
            SplittableRandom random) {
        Location current = blockLocation(Objects.requireNonNull(targetLocation, "targetLocation"));
        Vector velocity =
                Objects.requireNonNull(targetVelocity, "targetVelocity").clone();
        Vector look = Objects.requireNonNull(lookDirection, "lookDirection").clone();
        Objects.requireNonNull(random, "random");

        Vector horizontalVelocity = velocity.clone().setY(0.0D);
        if (horizontalVelocity.lengthSquared() > 0.36D) {
            horizontalVelocity.normalize().multiply(0.6D);
        }
        Location predicted = current.clone()
                .add(
                        horizontalVelocity.getX() * 3.0D,
                        !onGround && velocity.getY() > 0.05D ? Math.min(1.0D, velocity.getY() * 2.0D) : 0.0D,
                        horizontalVelocity.getZ() * 3.0D);
        predicted = blockLocation(predicted);

        Vector direction = horizontalVelocity.lengthSquared() >= 0.0025D
                ? horizontalVelocity.normalize()
                : look.setY(0.0D).normalize();
        if (!Double.isFinite(direction.getX())
                || !Double.isFinite(direction.getZ())
                || direction.lengthSquared() < 0.001D) {
            direction = new Vector(1.0D, 0.0D, 0.0D);
        }
        Vector forward = cardinal(direction);
        Vector side = new Vector(-forward.getZ(), 0.0D, forward.getX());

        List<Location> candidates = new ArrayList<>();
        if (!onGround || velocity.getY() > 0.08D) {
            candidates.add(predicted.clone().add(0.0D, 1.0D, 0.0D));
            candidates.add(predicted);
            candidates.add(current.clone().add(0.0D, 1.0D, 0.0D));
        } else {
            candidates.add(current);
            candidates.add(predicted);
        }
        candidates.add(predicted.clone().add(forward));
        if (random.nextBoolean()) {
            candidates.add(predicted.clone().add(side));
            candidates.add(predicted.clone().subtract(side));
        } else {
            candidates.add(predicted.clone().subtract(side));
            candidates.add(predicted.clone().add(side));
        }
        candidates.add(predicted.clone().add(0.0D, 1.0D, 0.0D));
        candidates.add(current.clone().subtract(forward));

        LinkedHashMap<Position, Location> unique = new LinkedHashMap<>();
        for (Location candidate : candidates) {
            Location candidateBlock = blockLocation(candidate);
            unique.putIfAbsent(Position.from(candidateBlock), candidateBlock);
        }
        return com.monkey.ultimatebot.common.util.ImmutableCollections.copyOf(unique.values());
    }

    private static Location blockLocation(Location location) {
        return new Location(
                Objects.requireNonNull(location.getWorld(), "location world"),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ());
    }

    private static Vector cardinal(Vector direction) {
        int x = Math.abs(direction.getX()) >= 0.38D ? (int) Math.signum(direction.getX()) : 0;
        int z = Math.abs(direction.getZ()) >= 0.38D ? (int) Math.signum(direction.getZ()) : 0;
        return new Vector(x, 0.0D, z);
    }

    public static final class Position {
        private final java.util.UUID worldUUID;
        private final int x;
        private final int y;
        private final int z;

        public Position(java.util.UUID worldUUID, int x, int y, int z) {
            this.worldUUID = worldUUID;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public java.util.UUID worldUUID() {
            return worldUUID;
        }
        public int x() {
            return x;
        }
        public int y() {
            return y;
        }
        public int z() {
            return z;
        }

        public static Position from(Location location) {
            return new Position(
                    Objects.requireNonNull(location.getWorld(), "location world")
                            .getUID(),
                    location.getBlockX(),
                    location.getBlockY(),
                    location.getBlockZ());
        }
    

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Position)) {
                return false;
            }
            Position other = (Position) obj;
            return java.util.Objects.equals(worldUUID, other.worldUUID) && x == other.x && y == other.y && z == other.z;
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(worldUUID, x, y, z);
        }

        @Override
        public String toString() {
            return "Position[worldUUID=" + worldUUID + ", x=" + x + ", y=" + y + ", z=" + z + "]";
        }
    }
}
