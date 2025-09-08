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
        Vec3 botPos = bot.position();
        Vec3 crystalCenter = Vec3.atCenterOf(crystalPos.above());

        double distanceToTarget = crystalCenter.distanceTo(targetPos);
        double distanceToBot = crystalCenter.distanceTo(botPos);

        if (distanceToBot < minCrystalDistance) {
            return -200;
        }

        double score = 0;

        double targetDamage = getCachedDamage(distanceToTarget);
        double botDamage = getCachedDamage(distanceToBot);

        score += targetDamage * 55;
        score -= botDamage * 40;

        if (distanceToTarget <= optimalDamageRange) {
            score += (optimalDamageRange - distanceToTarget) * 25;
        }

        if (distanceToBot < 3.5) {
            score -= (3.5 - distanceToBot) * 25;
        }

        int yDiff = crystalPos.getY() - target.blockPosition().getY();

        if (yDiff == -2) score += 80;
        else if (yDiff == -1) score += 100;
        else if (yDiff == 0) score += 50;
        else if (yDiff == 1) score += 15;
        else if (yDiff < -2) {
            score += 70 + Math.min(Math.abs(yDiff) * 15, 60);
        } else {
            score -= Math.abs(yDiff) * 40;
        }

        int existingCount = crystalCountAtPosition.getOrDefault(crystalPos, 0);
        if (existingCount > 0 && distanceToBot > 4.0) {
            score += existingCount * 20;
        }

        if (yDiff < 0) {
            double heightBonus = Math.abs(yDiff) * 30;
            if (yDiff >= -3 && yDiff <= -1) {
                heightBonus *= 1.5;
            }
            score += heightBonus;
        }

        if (!target.onGround() && yDiff < 0) {
            score += 40;
        }

        return score;
    }

    public double evaluateCrystalForAttack(EndCrystal crystal, Player target,
                                           Set<EndCrystal> myPlacedCrystals,
                                           double crystalAttackRange,
                                           double minCrystalDistance,
                                           double optimalDamageRange) {
        Vec3 crystalPos = crystal.position();
        double distanceToTarget = target.position().distanceTo(crystalPos);
        double distanceToBot = bot.position().distanceTo(crystalPos);

        if (distanceToBot > crystalAttackRange) return -1;
        if (distanceToBot < minCrystalDistance - 0.5) return -0.5;

        double score = 0;

        if (distanceToTarget <= optimalDamageRange) {
            score = (optimalDamageRange - distanceToTarget) / optimalDamageRange;
        }

        if (myPlacedCrystals.contains(crystal)) {
            score += 0.5;
        }

        if (distanceToTarget <= 3.0) {
            score += 0.4;
        }

        double targetY = target.position().y;
        double crystalY = crystalPos.y;
        double yDiff = crystalY - targetY;

        if (yDiff < -0.5) {
            score += 0.3;
        }
        if (yDiff < -1.5) {
            score += 0.2;
        }

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