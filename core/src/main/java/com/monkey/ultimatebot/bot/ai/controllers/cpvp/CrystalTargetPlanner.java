package com.monkey.ultimatebot.bot.ai.controllers.cpvp;

import com.monkey.ultimatebot.bot.ai.controllers.cpvp.helper.crystal.CrystalManager;
import com.monkey.ultimatebot.bot.ai.controllers.cpvp.helper.crystal.CrystalPositionEvaluator;
import com.monkey.ultimatebot.bot.ai.difficulty.configs.CPVPConfig;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.bukkit.World;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;

@SuppressWarnings("NullAway")
final class CrystalTargetPlanner {

    private final Player bot;
    private final World world;
    private final CrystalManager crystalManager;
    private final CrystalPositionEvaluator positionEvaluator;

    CrystalTargetPlanner(Player bot, CrystalManager crystalManager, CrystalPositionEvaluator positionEvaluator) {
        this.bot = Objects.requireNonNull(bot, "bot");
        this.world = bot.getWorld();
        this.crystalManager = Objects.requireNonNull(crystalManager, "crystalManager");
        this.positionEvaluator = Objects.requireNonNull(positionEvaluator, "positionEvaluator");
    }

    List<EnderCrystal> collectAttackCandidates(Player target, Set<EnderCrystal> botCrystals, CPVPConfig config) {
        Set<EnderCrystal> unique = new LinkedHashSet<>();
        botCrystals.stream()
                .filter(crystal -> !crystal.isDead() && crystal.isValid())
                .forEach(unique::add);
        crystalManager.findNearbyCrystals(bot, world, config.getCrystalAttackRange()).stream()
                .filter(crystal -> !crystal.isDead() && crystal.isValid())
                .forEach(unique::add);

        List<EnderCrystal> candidates = new ArrayList<>(unique);
        candidates.sort((first, second) ->
                Double.compare(score(second, target, botCrystals, config), score(first, target, botCrystals, config)));
        return candidates;
    }

    boolean shouldForceObsidianPlacement(
            Player target,
            Map<BlockVector, Long> obsidianCache,
            Map<BlockVector, Integer> crystalCounts,
            Map<BlockVector, Long> recentUsage,
            CPVPConfig config,
            CrystalCombatPolicy policy) {
        List<BlockVector> positions = crystalManager.getValidCrystalPositions(
                obsidianCache, target, bot, world, config.getMaxCrystalDistance());
        if (positions.isEmpty()) {
            return true;
        }

        long now = System.currentTimeMillis();
        double threshold = config.getMinCrystalScore() + policy.strongScoreOffset();
        int strongPositions = 0;
        for (BlockVector position : positions) {
            if (isCoolingDown(position, recentUsage, policy, now)) {
                continue;
            }
            double score = positionEvaluator.calculateCrystalScore(
                    position, target, crystalCounts, config.getOptimalDamageRange(), config.getMinCrystalDistance());
            if (score >= threshold && ++strongPositions >= policy.desiredStrongPositionCount()) {
                return false;
            }
        }
        return true;
    }

    void pruneObsidianCache(Player target, Map<BlockVector, Long> obsidianCache, CrystalCombatPolicy policy) {
        long now = System.currentTimeMillis();
        obsidianCache
                .entrySet()
                .removeIf(entry -> target.getLocation().distance(entry.getKey().toLocation(world))
                                > policy.maxUsefulTargetDistance()
                        && now - entry.getValue() > 500L);
    }

    boolean isCoolingDown(
            BlockVector position, Map<BlockVector, Long> recentUsage, CrystalCombatPolicy policy, long now) {
        Long lastUsed = recentUsage.get(position);
        return lastUsed != null && now - lastUsed < policy.positionReuseDelayMs();
    }

    private double score(EnderCrystal crystal, Player target, Set<EnderCrystal> botCrystals, CPVPConfig config) {
        return positionEvaluator.evaluateCrystalForAttack(
                crystal,
                target,
                botCrystals,
                config.getCrystalAttackRange(),
                config.getMinCrystalDistance(),
                config.getOptimalDamageRange());
    }
}
