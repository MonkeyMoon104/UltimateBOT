package com.monkey.ultimatebot.bot.ai.controllers.cpvp.helper.crystal;

import com.monkey.ultimatebot.bot.ai.controllers.combat.ExplosionDamageEstimator;
import com.monkey.ultimatebot.compat.MinecraftVersionAccess;
import java.util.Map;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;

@SuppressWarnings("NullAway")
public class CrystalPositionEvaluator {

    private final Player bot;
    private final World world;

    public CrystalPositionEvaluator(Player bot) {
        this.bot = bot;
        this.world = bot.getWorld();
    }

    public double calculateCrystalScore(
            BlockVector crystalPos,
            Player target,
            Map<BlockVector, Integer> crystalCountAtPosition,
            double optimalDamageRange,
            double minCrystalDistance) {
        Location explosionPos =
                new Location(
                        world,
                        crystalPos.getBlockX() + 0.5D,
                        crystalPos.getBlockY() + 1.5D,
                        crystalPos.getBlockZ() + 0.5D);

        double targetDamage = estimateDamage(explosionPos, target);
        double selfDamage = estimateDamage(explosionPos, bot);
        if (targetDamage <= 0.2D || selfDamage >= bot.getHealth() - 1.0D) {
            return 0.0D;
        }

        double distanceToTarget = explosionPos.distance(target.getLocation());
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
            EnderCrystal crystal,
            Player target,
            Set<EnderCrystal> myPlacedCrystals,
            double crystalAttackRange,
            double minCrystalDistance,
            double optimalDamageRange) {
        Location crystalPos = crystal.getLocation();
        double distanceToBot = crystalPos.distance(bot.getLocation());
        if (distanceToBot > crystalAttackRange) {
            return 0.0D;
        }

        double targetDamage = estimateDamage(crystalPos, target);
        double selfDamage = estimateDamage(crystalPos, bot);
        if (targetDamage <= 0.2D || selfDamage >= bot.getHealth() - 0.5D) {
            return 0.0D;
        }

        double distanceToTarget = target.getLocation().distance(crystalPos);
        double rangeBonus = Math.max(0.0D, crystalAttackRange - distanceToBot) * 0.25D;
        double targetProximityBonus = Math.max(0.0D, 4.0D - distanceToTarget) * 0.6D;
        double ownershipBonus = myPlacedCrystals.contains(crystal) ? 1.5D : 0.0D;
        double score =
                (targetDamage * 3.0D)
                        - (selfDamage * 2.8D)
                        + rangeBonus
                        + targetProximityBonus
                        + ownershipBonus;
        if (targetDamage > target.getHealth()) {
            score += 3.5D;
        }
        return Math.max(0.0D, score);
    }

    /**
     * Paper 1.13 ray-trace exposure often underestimates (or zeros) damage and then CPvP never
     * places/attacks. Use full-exposure falloff there only.
     */
    private static double estimateDamage(Location explosionPos, Player entity) {
        if (MinecraftVersionAccess.is1_13()) {
            return estimateDamageFullExposure(explosionPos, entity, 6.0D);
        }
        return ExplosionDamageEstimator.estimateCrystalDamage(explosionPos, entity);
    }

    private static double estimateDamageFullExposure(
            Location explosionLocation, Player entity, double explosionPower) {
        double maxRadius = explosionPower * 2.0D;
        double distance = explosionLocation.distance(entity.getLocation());
        if (distance > maxRadius) {
            return 0.0D;
        }
        double impact = 1.0D - (distance / maxRadius);
        if (impact <= 0.0D) {
            return 0.0D;
        }
        return ((impact * impact + impact) * 7.0D * explosionPower) + 1.0D;
    }
}
