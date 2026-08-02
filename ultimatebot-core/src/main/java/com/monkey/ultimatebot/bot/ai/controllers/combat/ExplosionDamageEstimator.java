package com.monkey.ultimatebot.bot.ai.controllers.combat;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class ExplosionDamageEstimator {

    private static final double CRYSTAL_POWER = 6.0D;
    private static final double ANCHOR_POWER = 5.0D;
    private static final int EXPOSURE_SAMPLES_PER_AXIS = 3;

    private ExplosionDamageEstimator() {}

    public static double estimateCrystalDamage(Level level, Vec3 explosionPos, Player entity) {
        return estimateExplosionDamage(level, explosionPos, entity, CRYSTAL_POWER);
    }

    public static double estimateAnchorDamage(Level level, Vec3 explosionPos, Player entity) {
        return estimateExplosionDamage(level, explosionPos, entity, ANCHOR_POWER);
    }

    private static double estimateExplosionDamage(
            Level level, Vec3 explosionPos, Player entity, double explosionPower) {
        double maxRadius = explosionPower * 2.0D;
        double distance = explosionPos.distanceTo(entity.position());
        if (distance > maxRadius) {
            return 0.0D;
        }

        double exposure = estimateExposure(level, explosionPos, entity);
        double impact = (1.0D - (distance / maxRadius)) * exposure;
        if (impact <= 0.0D) {
            return 0.0D;
        }

        return ((impact * impact + impact) * 7.0D * explosionPower) + 1.0D;
    }

    private static double estimateExposure(Level level, Vec3 explosionPos, Player entity) {
        AABB bb = entity.getBoundingBox();
        int visible = 0;
        int total = 0;

        int steps = EXPOSURE_SAMPLES_PER_AXIS - 1;
        for (int x = 0; x <= steps; x++) {
            double sampleX = Mth.lerp((double) x / steps, bb.minX, bb.maxX);
            for (int y = 0; y <= steps; y++) {
                double sampleY = Mth.lerp((double) y / steps, bb.minY, bb.maxY);
                for (int z = 0; z <= steps; z++) {
                    double sampleZ = Mth.lerp((double) z / steps, bb.minZ, bb.maxZ);
                    Vec3 samplePoint = new Vec3(sampleX, sampleY, sampleZ);

                    ClipContext context = new ClipContext(
                            samplePoint, explosionPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity);

                    HitResult result = level.clip(context);
                    if (result.getType() == HitResult.Type.MISS) {
                        visible++;
                    }
                    total++;
                }
            }
        }

        return total == 0 ? 0.0D : (double) visible / total;
    }
}
