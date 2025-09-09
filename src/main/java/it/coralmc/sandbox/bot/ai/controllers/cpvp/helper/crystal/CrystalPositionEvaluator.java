package it.coralmc.sandbox.bot.ai.controllers.cpvp.helper.crystal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.Set;

public class CrystalPositionEvaluator {

    private final Player bot;

    private static final double[] DAMAGE_CACHE = new double[200];

    static {
        for (int i = 0; i < DAMAGE_CACHE.length; i++) {
            double distance = i * 0.1;
            DAMAGE_CACHE[i] = calculateDamageForDistance(distance);
        }
    }

    public CrystalPositionEvaluator(Player bot) {
        this.bot = bot;
    }

    public double calculateCrystalScore(BlockPos crystalPos, Player target,
                                        Map<BlockPos, Integer> crystalCountAtPosition,
                                        double optimalDamageRange,
                                        double minCrystalDistance) {
        Vec3 targetPos = target.position();
        Vec3 crystalCenter = Vec3.atCenterOf(crystalPos.above());

        double distanceToTarget = crystalCenter.distanceTo(targetPos);

        double score = 0;

        double targetDamage = getCachedDamage(distanceToTarget);
        score += targetDamage * 55;

        if (distanceToTarget <= optimalDamageRange) {
            score += (optimalDamageRange - distanceToTarget) * 25;
        }

        int yDiff = crystalPos.getY() - target.blockPosition().getY();
        if (yDiff < 0) {
            score += Math.abs(yDiff) * 50;
        } else {
            score += 10;
        }

        int existingCount = crystalCountAtPosition.getOrDefault(crystalPos, 0);
        score += existingCount * 20;

        return score;
    }

    public double evaluateCrystalForAttack(EndCrystal crystal, Player target,
                                           Set<EndCrystal> myPlacedCrystals,
                                           double crystalAttackRange,
                                           double minCrystalDistance,
                                           double optimalDamageRange) {

        Vec3 crystalPos = crystal.position();
        double distanceToTarget = target.position().distanceTo(crystalPos);

        double score = 1.0;

        double distanceToBot = crystalPos.distanceTo(bot.position());
        score -= Math.max(0, 3.0 - distanceToBot) * 20;

        if (distanceToTarget <= optimalDamageRange) {
            score += (optimalDamageRange - distanceToTarget) / optimalDamageRange;
        }

        if (myPlacedCrystals.contains(crystal)) score += 0.5;
        if (distanceToTarget <= 3.0) score += 0.4;

        double yDiff = crystalPos.y - target.position().y;
        if (yDiff < 0) score += Math.abs(yDiff) * 0.3;

        return score;
    }

    private double getCachedDamage(double distance) {
        if (distance > 20.0) return 0.0;
        int index = (int) Math.min(distance * 10, DAMAGE_CACHE.length - 1);
        return DAMAGE_CACHE[index];
    }

    private static double calculateDamageForDistance(double distance) {
        if (distance > 12.0) return 0.0;
        double maxDamage = 14.0;
        double falloff = Math.max(0.0, 1.0 - (distance / 12.0));
        return maxDamage * falloff * falloff * falloff;
    }
}
