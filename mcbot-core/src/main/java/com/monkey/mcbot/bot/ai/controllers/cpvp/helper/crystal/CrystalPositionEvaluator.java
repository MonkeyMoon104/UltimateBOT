package com.monkey.mcbot.bot.ai.controllers.cpvp.helper.crystal;

import com.monkey.mcbot.bot.ai.controllers.combat.ExplosionDamageEstimator;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class CrystalPositionEvaluator {

    private final Player bot;
    private final Level level;

    public CrystalPositionEvaluator(Player bot, Level level) {
        this.bot = bot;
        this.level = level;
    }

    public double calculateCrystalScore(
            BlockPos crystalPos,
            Player target,
            Map<BlockPos, Integer> crystalCountAtPosition,
            double optimalDamageRange,
            double minCrystalDistance) {
        Vec3 explosionPos = Vec3.atCenterOf(crystalPos.above());

        double targetDamage = ExplosionDamageEstimator.estimateCrystalDamage(level, explosionPos, target);
        double selfDamage = ExplosionDamageEstimator.estimateCrystalDamage(level, explosionPos, bot);

        if (targetDamage <= 0.2D) {
            return 0.0D;
        }

        if (selfDamage >= bot.getHealth() - 1.0F) {
            return 0.0D;
        }

        double distanceToTarget = explosionPos.distanceTo(target.position());
        double desiredDistance = Math.max(minCrystalDistance, Math.min(optimalDamageRange, 4.5D));
        double distancePenalty = Math.abs(distanceToTarget - desiredDistance) * 0.6D;

        int priorPlacementsAtPos = crystalCountAtPosition.getOrDefault(crystalPos, 0);
        double repetitionPenalty = Math.min(6, priorPlacementsAtPos) * 1.1D;

        double score = (targetDamage * 3.2D) - (selfDamage * 2.6D) - distancePenalty - repetitionPenalty;

        if (targetDamage > target.getHealth()) {
            score += 4.0D;
        }
        if (selfDamage < 2.5D) {
            score += 1.2D;
        }

        return Math.max(0.0D, score);
    }

    public double evaluateCrystalForAttack(
            EndCrystal crystal,
            Player target,
            Set<EndCrystal> myPlacedCrystals,
            double crystalAttackRange,
            double minCrystalDistance,
            double optimalDamageRange) {
        Vec3 crystalPos = crystal.position();
        double distanceToBot = crystalPos.distanceTo(bot.position());
        if (distanceToBot > crystalAttackRange) {
            return 0.0D;
        }

        double targetDamage = ExplosionDamageEstimator.estimateCrystalDamage(level, crystalPos, target);
        double selfDamage = ExplosionDamageEstimator.estimateCrystalDamage(level, crystalPos, bot);

        if (targetDamage <= 0.2D) {
            return 0.0D;
        }

        if (selfDamage >= bot.getHealth() - 0.5F) {
            return 0.0D;
        }

        double distanceToTarget = target.position().distanceTo(crystalPos);
        double rangeBonus = Math.max(0.0D, crystalAttackRange - distanceToBot) * 0.25D;
        double targetProximityBonus = Math.max(0.0D, 4.0D - distanceToTarget) * 0.6D;
        double ownershipBonus = myPlacedCrystals.contains(crystal) ? 1.5D : 0.0D;

        double score = (targetDamage * 3.0D) - (selfDamage * 2.8D) + rangeBonus + targetProximityBonus + ownershipBonus;

        if (targetDamage > target.getHealth()) {
            score += 3.5D;
        }

        return Math.max(0.0D, score);
    }
}
