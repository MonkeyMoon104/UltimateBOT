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
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

final class CrystalTargetPlanner {

    private final Player bot;
    private final Level level;
    private final CrystalManager crystalManager;
    private final CrystalPositionEvaluator positionEvaluator;

    CrystalTargetPlanner(Player bot, CrystalManager crystalManager, CrystalPositionEvaluator positionEvaluator) {
        this.bot = Objects.requireNonNull(bot, "bot");
        this.level = bot.level();
        this.crystalManager = Objects.requireNonNull(crystalManager, "crystalManager");
        this.positionEvaluator = Objects.requireNonNull(positionEvaluator, "positionEvaluator");
    }

    List<EndCrystal> collectAttackCandidates(Player target, Set<EndCrystal> botCrystals, CPVPConfig config) {
        Set<EndCrystal> unique = new LinkedHashSet<>();
        botCrystals.stream().filter(EndCrystal::isAlive).forEach(unique::add);
        crystalManager.findNearbyCrystals(bot, level, config.getCrystalAttackRange()).stream()
                .filter(EndCrystal::isAlive)
                .forEach(unique::add);

        List<EndCrystal> candidates = new ArrayList<>(unique);
        candidates.sort((first, second) ->
                Double.compare(score(second, target, botCrystals, config), score(first, target, botCrystals, config)));
        return candidates;
    }

    boolean shouldForceObsidianPlacement(
            Player target,
            Map<BlockPos, Long> obsidianCache,
            Map<BlockPos, Integer> crystalCounts,
            Map<BlockPos, Long> recentUsage,
            CPVPConfig config,
            CrystalCombatPolicy policy) {
        List<BlockPos> positions = crystalManager.getValidCrystalPositions(
                obsidianCache, target, bot, level, config.getMaxCrystalDistance());
        if (positions.isEmpty()) {
            return true;
        }

        long now = System.currentTimeMillis();
        double threshold = config.getMinCrystalScore() + policy.strongScoreOffset();
        int strongPositions = 0;
        for (BlockPos position : positions) {
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

    void pruneObsidianCache(Player target, Map<BlockPos, Long> obsidianCache, CrystalCombatPolicy policy) {
        long now = System.currentTimeMillis();
        obsidianCache
                .entrySet()
                .removeIf(entry ->
                        target.position().distanceTo(Vec3.atCenterOf(entry.getKey())) > policy.maxUsefulTargetDistance()
                                && now - entry.getValue() > 500L);
    }

    boolean isCoolingDown(BlockPos position, Map<BlockPos, Long> recentUsage, CrystalCombatPolicy policy, long now) {
        Long lastUsed = recentUsage.get(position);
        return lastUsed != null && now - lastUsed < policy.positionReuseDelayMs();
    }

    private double score(EndCrystal crystal, Player target, Set<EndCrystal> botCrystals, CPVPConfig config) {
        return positionEvaluator.evaluateCrystalForAttack(
                crystal,
                target,
                botCrystals,
                config.getCrystalAttackRange(),
                config.getMinCrystalDistance(),
                config.getOptimalDamageRange());
    }
}
