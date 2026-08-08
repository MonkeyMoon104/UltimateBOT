package com.monkey.ultimatebot.bot.ai.controllers.combat;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

@SuppressWarnings("NullAway")
public final class ExplosionDamageEstimator {

    private static final double CRYSTAL_POWER = 6.0D;
    private static final double ANCHOR_POWER = 5.0D;
    private static final int EXPOSURE_SAMPLES_PER_AXIS = 3;

    private ExplosionDamageEstimator() {}

    public static double estimateCrystalDamage(Location explosionLocation, Player entity) {
        return estimateExplosionDamage(explosionLocation, entity, CRYSTAL_POWER);
    }

    public static double estimateAnchorDamage(Location explosionLocation, Player entity) {
        return estimateExplosionDamage(explosionLocation, entity, ANCHOR_POWER);
    }

    private static double estimateExplosionDamage(Location explosionLocation, Player entity, double explosionPower) {
        double maxRadius = explosionPower * 2.0D;
        double distance = explosionLocation.distance(entity.getLocation());
        if (distance > maxRadius) {
            return 0.0D;
        }

        double exposure = estimateExposure(explosionLocation, entity);
        double impact = (1.0D - (distance / maxRadius)) * exposure;
        if (impact <= 0.0D) {
            return 0.0D;
        }

        return ((impact * impact + impact) * 7.0D * explosionPower) + 1.0D;
    }

    private static double estimateExposure(Location explosionLocation, Player entity) {
        BoundingBox bb = entity.getBoundingBox();
        int visible = 0;
        int total = 0;
        int steps = EXPOSURE_SAMPLES_PER_AXIS - 1;

        for (int x = 0; x <= steps; x++) {
            double sampleX = lerp((double) x / steps, bb.getMinX(), bb.getMaxX());
            for (int y = 0; y <= steps; y++) {
                double sampleY = lerp((double) y / steps, bb.getMinY(), bb.getMaxY());
                for (int z = 0; z <= steps; z++) {
                    double sampleZ = lerp((double) z / steps, bb.getMinZ(), bb.getMaxZ());
                    Location sample = new Location(entity.getWorld(), sampleX, sampleY, sampleZ);
                    Vector direction = explosionLocation.toVector().subtract(sample.toVector());
                    double distance = direction.length();
                    if (distance < 1.0E-6D) {
                        visible++;
                    } else {
                        RayTraceResult result = entity.getWorld().rayTraceBlocks(
                                sample, direction.normalize(), distance, FluidCollisionMode.NEVER, true);
                        if (result == null || result.getHitBlock() == null) {
                            visible++;
                        }
                    }
                    total++;
                }
            }
        }

        return total == 0 ? 0.0D : (double) visible / total;
    }

    private static double lerp(double delta, double start, double end) {
        return start + delta * (end - start);
    }
}
