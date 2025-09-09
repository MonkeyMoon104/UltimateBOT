package it.coralmc.sandbox.bot.ai.controllers.cpvp.helper.obsidian;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
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
        recentPlacements.entrySet().removeIf(entry -> currentTime - entry.getValue() > positionCooldownMs);
    }

    private boolean isValidObsidianPosition(BlockPos pos, Player target) {
        if (!level.getBlockState(pos.below()).isSolid()) return false;

        if (!level.getBlockState(pos.above()).isAir() || !level.getBlockState(pos.above(2)).isAir())
            return false;

        double distanceToBot = bot.position().distanceTo(Vec3.atCenterOf(pos));
        double distanceToTarget = target.position().distanceTo(Vec3.atCenterOf(pos.above()));

        return distanceToBot <= 10.0 && distanceToTarget <= 10.0;
    }

    private double calculatePositionScore(BlockPos pos, Player target) {
        Vec3 crystalPos = Vec3.atCenterOf(pos.above());
        double distanceToTarget = target.position().distanceTo(crystalPos);
        return Math.max(0, 15 - distanceToTarget);
    }
}
