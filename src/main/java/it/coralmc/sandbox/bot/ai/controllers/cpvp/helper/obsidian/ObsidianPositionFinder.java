package it.coralmc.sandbox.bot.ai.controllers.cpvp.helper.obsidian;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class ObsidianPositionFinder {

    private final Player bot;
    private final Level level;

    private final Map<BlockPos, Double> scoreCache = new HashMap<>();
    private long lastCacheReset = 0;
    private static final long CACHE_RESET_INTERVAL_MS = 2000;

    public ObsidianPositionFinder(Player bot, Level level) {
        this.bot = bot;
        this.level = level;
    }

    public List<BlockPos> findBestObsidianPositions(Player target, int maxPositions,
                                                    Map<BlockPos, Long> recentPlacements,
                                                    List<BlockPos> cachedValidPositions,
                                                    long lastPositionCache,
                                                    long positionCacheMs) {
        cleanupRecentPlacements(recentPlacements);

        long currentTime = System.currentTimeMillis();

        if (currentTime - lastCacheReset > CACHE_RESET_INTERVAL_MS) {
            scoreCache.clear();
            lastCacheReset = currentTime;
        }

        if (currentTime - lastPositionCache > positionCacheMs) {
            cachedValidPositions.clear();
            BlockPos targetBlockPos = target.blockPosition();

            for (int y = -5; y <= 3; y++) {
                for (int x = -7; x <= 7; x++) {
                    for (int z = -7; z <= 7; z++) {
                        BlockPos checkPos = targetBlockPos.offset(x, y, z);
                        if (recentPlacements.containsKey(checkPos)) continue;
                        if (isValidObsidianPosition(checkPos, target)) {
                            cachedValidPositions.add(checkPos);
                        }
                    }
                }
            }
        }

        if (cachedValidPositions.isEmpty()) return new ArrayList<>();

        return cachedValidPositions.stream()
                .sorted((pos1, pos2) -> Double.compare(
                        calculatePositionScore(pos2, target),
                        calculatePositionScore(pos1, target)
                ))
                .limit(maxPositions)
                .toList();
    }

    private void cleanupRecentPlacements(Map<BlockPos, Long> recentPlacements) {
        long currentTime = System.currentTimeMillis();
        long positionCooldownMs = 1500;
        recentPlacements.entrySet().removeIf(entry ->
                currentTime - entry.getValue() > positionCooldownMs);
    }

    private boolean isValidObsidianPosition(BlockPos pos, Player target) {
        BlockState currentState = level.getBlockState(pos);
        if (!currentState.canBeReplaced()) return false;

        BlockState below = level.getBlockState(pos.below());
        if (!below.isSolid()) return false;

        if (!level.getBlockState(pos.above()).isAir() || !level.getBlockState(pos.above(2)).isAir())
            return false;

        double distanceToBot = bot.position().distanceTo(Vec3.atCenterOf(pos));
        double distanceToTarget = target.position().distanceTo(Vec3.atCenterOf(pos.above()));

        double maxCrystalDistance = 8.0;
        return distanceToBot <= 6.5 && distanceToTarget <= maxCrystalDistance;
    }

    private double calculatePositionScore(BlockPos pos, Player target) {
        Double cachedScore = scoreCache.get(pos);
        if (cachedScore != null) {
            return cachedScore;
        }

        Vec3 targetPos = target.position();
        Vec3 botPos = bot.position();
        int targetY = target.blockPosition().getY();

        double score = 0;
        double optimalDamageRange = 6.0;
        double minCrystalDistance = 2.5;

        Vec3 crystalPos = Vec3.atCenterOf(pos.above());
        double distanceToTarget = targetPos.distanceTo(crystalPos);
        double distanceToBot = botPos.distanceTo(crystalPos);

        if (distanceToTarget <= optimalDamageRange) {
            score += (optimalDamageRange - distanceToTarget) * 35;
        }

        if (distanceToBot > minCrystalDistance) {
            if (distanceToBot > 6.0) score -= (distanceToBot - 6.0) * 20;
        } else {
            score -= 150;
        }

        int yDiff = pos.getY() - targetY;

        if (yDiff == -2) score += 120;
        else if (yDiff == -1) score += 100;
        else if (yDiff == 0) score += 70;
        else if (yDiff == 1) score += 30;
        else if (yDiff < -2) score += 90 + Math.abs(yDiff) * 10;
        else score -= Math.abs(yDiff) * 35;

        if (hasNearbySupport(pos)) score += 25;

        if (!target.onGround()) {
            if (yDiff < 0) score += 50;
        } else {
            if (yDiff == -1) score += 60;
        }

        if (distanceToTarget >= 2.0 && distanceToTarget <= 4.0) {
            score += 40;
        }

        scoreCache.put(pos, score);
        return score;
    }

    private boolean hasNearbySupport(BlockPos pos) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos checkPos = pos.relative(dir);
            if (level.getBlockState(checkPos).isSolid()) return true;
        }
        return false;
    }
}