package it.coralmc.sandbox.bot.ai.controllers.cpvp;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.Set;

public class CrystalPositionEvaluator {

    private final Player bot;

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
            return -1000.0;
        }

        double score = 0;

        double targetDamage = calculatePredictedDamage(distanceToTarget);
        double botDamage = calculatePredictedDamage(distanceToBot);

        score += targetDamage * 40;
        score -= botDamage * 60;

        if (distanceToTarget <= optimalDamageRange) {
            score += (optimalDamageRange - distanceToTarget) * 20;
        }

        if (distanceToBot < 4.0) {
            score -= (4.0 - distanceToBot) * 40;
        }

        int yDiff = crystalPos.getY() - target.blockPosition().getY();
        if (yDiff == -1) score += 35;
        else if (yDiff == 0) score += 25;
        else if (yDiff < -1) score += Math.abs(yDiff) * 12;

        int existingCount = crystalCountAtPosition.getOrDefault(crystalPos, 0);
        if (existingCount > 0 && distanceToBot > 4.0) {
            score += existingCount * 15;
        }
        if (yDiff < 0) {
            score += Math.abs(yDiff) * 50;
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
        if (distanceToBot < minCrystalDistance) return -1;

        double score = 0;

        if (distanceToTarget <= optimalDamageRange) {
            score = (optimalDamageRange - distanceToTarget) / optimalDamageRange;
        }

        if (myPlacedCrystals.contains(crystal)) {
            score += 0.4;
        }

        if (distanceToTarget <= 3.0) {
            score += 0.3;
        }

        return score;
    }

    private double calculatePredictedDamage(double distance) {
        if (distance > 12.0) return 0.0;

        double maxDamage = 14.0;
        double falloff = Math.max(0.0, 1.0 - (distance / 12.0));

        return maxDamage * falloff * falloff;
    }
}