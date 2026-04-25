package com.monkey.mcbot.bot.ai.controllers.cpvp.helper.obsidian;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ObsidianPositionFinder {

    private final Player bot;
    private final Level level;

    public ObsidianPositionFinder(Player bot, Level level) {
        this.bot = bot;
        this.level = level;
    }

    public List<BlockPos> findBestObsidianPositions(Player target, int maxPositions,
                                                    Map<BlockPos, Long> recentPlacements,
                                                    long positionCooldownMs,
                                                    double maxPlacementDistance) {

        cleanupRecentPlacements(recentPlacements, positionCooldownMs);

        List<BlockPos> validPositions = new ArrayList<>();
        BlockPos targetBlockPos = target.blockPosition();
        int scanRadius = Math.max(4, (int) Math.ceil(maxPlacementDistance));

        for (int y = -2; y <= 1; y++) {
            for (int x = -scanRadius; x <= scanRadius; x++) {
                for (int z = -scanRadius; z <= scanRadius; z++) {
                    BlockPos checkPos = targetBlockPos.offset(x, y, z);

                    if (recentPlacements.containsKey(checkPos)) continue;

                    int obsidianY = checkPos.getY();
                    int botY = bot.blockPosition().getY();
                    int targetY = target.blockPosition().getY();

                    if (obsidianY >= targetY) continue;

                    if (botY >= obsidianY) continue;

                    if (obsidianY < targetY && botY < obsidianY) {
                        if (isValidObsidianPosition(checkPos, target, maxPlacementDistance)) {
                            validPositions.add(checkPos);
                        }
                    }
                }
            }
        }

        validPositions.sort((pos1, pos2) -> {
            double score1 = evaluateObsidianScore(pos1, target);
            double score2 = evaluateObsidianScore(pos2, target);
            return Double.compare(score2, score1);
        });

        return validPositions.stream().limit(maxPositions).toList();
    }

    private void cleanupRecentPlacements(Map<BlockPos, Long> recentPlacements, long positionCooldownMs) {
        long currentTime = System.currentTimeMillis();
        recentPlacements.entrySet().removeIf(entry -> currentTime - entry.getValue() > positionCooldownMs);
    }

    private boolean isValidObsidianPosition(BlockPos pos, Player target, double maxPlacementDistance) {
        if (!level.getBlockState(pos).isAir()) return false;
        if (!level.getBlockState(pos.below()).isSolidRender()) return false;
        if (!level.getBlockState(pos.above()).isAir() || !level.getBlockState(pos.above(2)).isAir()) return false;

        double distanceToBot = bot.position().distanceTo(Vec3.atCenterOf(pos));
        double distanceToTarget = target.position().distanceTo(Vec3.atCenterOf(pos));
        double maxTargetDistance = Math.max(3.8D, Math.min(maxPlacementDistance - 1.5D, 5.2D));

        if (distanceToBot > maxPlacementDistance || distanceToTarget > maxTargetDistance) {
            return false;
        }

        int targetY = target.blockPosition().getY();
        if (Math.abs(pos.getY() - (targetY - 1)) > 2) {
            return false;
        }

        return hasLineOfSightFromBot(pos);
    }

    private double evaluateObsidianScore(BlockPos pos, Player target) {
        Vec3 posCenter = Vec3.atCenterOf(pos);
        double distanceToTarget = posCenter.distanceTo(target.position());
        double distanceToBot = posCenter.distanceTo(bot.position());

        double score = 0.0D;
        score -= Math.abs(distanceToTarget - 2.7D) * 2.8D;
        score -= Math.max(0.0D, distanceToBot - 4.8D) * 2.3D;
        score -= Math.max(0.0D, distanceToTarget - 4.2D) * 3.6D;

        int targetY = target.blockPosition().getY();
        int posY = pos.getY();
        score -= Math.abs((targetY - posY) - 1.0D) * 2.0D;

        if (hasLineOfSightFromBot(pos)) {
            score += 2.1D;
        }
        if (hasLineOfSightToTarget(pos, target)) {
            score += 1.6D;
        }

        return score;
    }

    private boolean hasLineOfSightFromBot(BlockPos pos) {
        return hasLineOfSight(bot.getEyePosition(1.0F), Vec3.atCenterOf(pos));
    }

    private boolean hasLineOfSightToTarget(BlockPos pos, Player target) {
        return hasLineOfSight(Vec3.atCenterOf(pos), target.getEyePosition(1.0F));
    }

    private boolean hasLineOfSight(Vec3 start, Vec3 end) {
        net.minecraft.world.level.ClipContext context = new net.minecraft.world.level.ClipContext(
                start,
                end,
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE,
                bot
        );

        net.minecraft.world.phys.BlockHitResult result = level.clip(context);
        return result.getType() == net.minecraft.world.phys.HitResult.Type.MISS;
    }
}
