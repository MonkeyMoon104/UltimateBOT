package it.coralmc.sandbox.bot.ai.controllers.cpvp.helper.obsidian;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class ObsidianPositionFinder {

    private final Player bot;
    private final Level level;

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

        List<BlockPos> validPositions = new ArrayList<>();
        BlockPos targetBlockPos = target.blockPosition();

        for (int y = -2; y <= 1; y++) {
            for (int x = -4; x <= 4; x++) {
                for (int z = -4; z <= 4; z++) {
                    BlockPos checkPos = targetBlockPos.offset(x, y, z);

                    if (recentPlacements.containsKey(checkPos)) continue;

                    int obsidianY = checkPos.getY();
                    int botY = bot.blockPosition().getY();
                    int targetY = target.blockPosition().getY();

                    if (obsidianY >= targetY) continue;

                    if (botY >= obsidianY) continue;

                    if (obsidianY < targetY && botY < obsidianY) {
                        if (isValidObsidianPosition(checkPos, target)) {
                            validPositions.add(checkPos);
                        }
                    }
                }
            }
        }

        validPositions.sort((pos1, pos2) -> {
            double dist1 = target.position().distanceTo(Vec3.atCenterOf(pos1));
            double dist2 = target.position().distanceTo(Vec3.atCenterOf(pos2));
            return Double.compare(dist1, dist2);
        });

        return validPositions.stream().limit(maxPositions).toList();
    }

    private void cleanupRecentPlacements(Map<BlockPos, Long> recentPlacements) {
        long currentTime = System.currentTimeMillis();
        long positionCooldownMs = 1500;
        recentPlacements.entrySet().removeIf(entry -> currentTime - entry.getValue() > positionCooldownMs);
    }

    private boolean isValidObsidianPosition(BlockPos pos, Player target) {
        if (!level.getBlockState(pos).isAir()) return false;
        if (!level.getBlockState(pos.below()).isSolid()) return false;
        if (!level.getBlockState(pos.above()).isAir() || !level.getBlockState(pos.above(2)).isAir()) return false;

        double distanceToBot = bot.position().distanceTo(Vec3.atCenterOf(pos));
        double distanceToTarget = target.position().distanceTo(Vec3.atCenterOf(pos));

        return distanceToBot <= 8.0 && distanceToTarget <= 8.0;
    }
}