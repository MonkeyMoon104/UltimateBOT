package com.monkey.ultimatebot.combat.mode.shared;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.random.RandomGenerator;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public final class WebTrapPlanner {
    private WebTrapPlanner() {}

    public static List<Location> plan(
            Location targetLocation,
            Vector targetVelocity,
            boolean onGround,
            Vector lookDirection,
            RandomGenerator random) {
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
        return List.copyOf(unique.values());
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

    public record Position(java.util.UUID worldUUID, int x, int y, int z) {
        public static Position from(Location location) {
            return new Position(
                    Objects.requireNonNull(location.getWorld(), "location world")
                            .getUID(),
                    location.getBlockX(),
                    location.getBlockY(),
                    location.getBlockZ());
        }
    }
}
