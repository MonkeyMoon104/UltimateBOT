package com.monkey.ultimatebot.combat.mode.cart;


import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.util.Vector;

final class CartPlacementPlanner {
    private CartPlacementPlanner() {}

    static List<Location> railCandidates(Location targetLocation, Vector targetVelocity) {
        Objects.requireNonNull(targetLocation, "targetLocation");
        Vector horizontalVelocity =
                Objects.requireNonNull(targetVelocity, "targetVelocity").clone().setY(0.0D);
        if (horizontalVelocity.lengthSquared() > 0.36D) {
            horizontalVelocity.normalize().multiply(0.6D);
        }
        LinkedHashMap<Position, Location> candidates = new LinkedHashMap<>();
        for (double predictionTicks : new double[] {2.0D, 1.0D, 0.0D}) {
            Location predicted =
                    targetLocation.clone().add(horizontalVelocity.clone().multiply(predictionTicks));
            for (int[] horizontalOffset :
                    new int[][] {{0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}}) {
                for (int yOffset : new int[] {0, -1, 1}) {
                    Location candidate = new Location(
                            Objects.requireNonNull(predicted.getWorld(), "candidate world"),
                            predicted.getBlockX() + horizontalOffset[0],
                            predicted.getBlockY() + yOffset,
                            predicted.getBlockZ() + horizontalOffset[1]);
                    candidates.putIfAbsent(Position.from(candidate), candidate);
                }
            }
        }
        return com.monkey.ultimatebot.common.util.ImmutableCollections.copyOf(candidates.values());
    }

    private static final class Position {
        private final java.util.UUID worldId;
        private final int x;
        private final int y;
        private final int z;

        private Position(java.util.UUID worldId, int x, int y, int z) {
            this.worldId = worldId;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        private static Position from(Location location) {
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
            return java.util.Objects.equals(worldId, other.worldId) && x == other.x && y == other.y && z == other.z;
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(worldId, x, y, z);
        }

        @Override
        public String toString() {
            return "Position[worldId=" + worldId + ", x=" + x + ", y=" + y + ", z=" + z + "]";
        }
    }
}
