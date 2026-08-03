package com.monkey.ultimatebot.combat.mode;

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
            for (int yOffset : new int[] {0, -1, 1}) {
                Location candidate = new Location(
                        Objects.requireNonNull(predicted.getWorld(), "candidate world"),
                        predicted.getBlockX(),
                        predicted.getBlockY() + yOffset,
                        predicted.getBlockZ());
                candidates.putIfAbsent(Position.from(candidate), candidate);
            }
        }
        return List.copyOf(candidates.values());
    }

    private record Position(java.util.UUID worldId, int x, int y, int z) {
        private static Position from(Location location) {
            return new Position(
                    Objects.requireNonNull(location.getWorld(), "location world")
                            .getUID(),
                    location.getBlockX(),
                    location.getBlockY(),
                    location.getBlockZ());
        }
    }
}
