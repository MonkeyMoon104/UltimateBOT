package com.monkey.ultimatebot.bot.ai.controllers.cpvp.helper.obsidian;

import java.util.stream.Collectors;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

@SuppressWarnings("NullAway")
public class ObsidianPositionFinder {

    private final ITrainingBot bot;

    public ObsidianPositionFinder(ITrainingBot bot) {
        this.bot = bot;
    }

    public List<BlockVector> findBestObsidianPositions(
            Player target,
            int maxPositions,
            Map<BlockVector, Long> recentPlacements,
            long positionCooldownMs,
            double maxPlacementDistance) {
        cleanupRecentPlacements(recentPlacements, positionCooldownMs);
        List<BlockVector> validPositions = new ArrayList<>();
        Vector targetBlockPos = target.getLocation().toVector();
        int scanRadius = Math.max(4, (int) Math.ceil(maxPlacementDistance));

        for (int y = -2; y <= 1; y++) {
            for (int x = -scanRadius; x <= scanRadius; x++) {
                for (int z = -scanRadius; z <= scanRadius; z++) {
                    BlockVector checkPos = new BlockVector(
                            targetBlockPos.getBlockX() + x, targetBlockPos.getBlockY() + y, targetBlockPos.getBlockZ() + z);
                    if (recentPlacements.containsKey(checkPos)) continue;
                    int obsidianY = checkPos.getBlockY();
                    int botY = bot.getLocation().getBlockY();
                    int targetY = target.getLocation().getBlockY();
                    if (obsidianY >= targetY || botY >= obsidianY) continue;
                    if (isValidObsidianPosition(checkPos, target, maxPlacementDistance)) {
                        validPositions.add(checkPos);
                    }
                }
            }
        }

        validPositions.sort((pos1, pos2) -> Double.compare(evaluateObsidianScore(pos2, target), evaluateObsidianScore(pos1, target)));
        return validPositions.stream().limit(maxPositions).collect(Collectors.toList());
    }

    private void cleanupRecentPlacements(Map<BlockVector, Long> recentPlacements, long positionCooldownMs) {
        long currentTime = System.currentTimeMillis();
        recentPlacements.entrySet().removeIf(entry -> currentTime - entry.getValue() > positionCooldownMs);
    }

    private boolean isValidObsidianPosition(BlockVector pos, Player target, double maxPlacementDistance) {
        if (!isPassable(blockAt(pos))) return false;
        if (!isSolid(blockAt(pos.getBlockX(), pos.getBlockY() - 1, pos.getBlockZ()))) return false;
        if (!isPassable(blockAt(pos.getBlockX(), pos.getBlockY() + 1, pos.getBlockZ()))
                || !isPassable(blockAt(pos.getBlockX(), pos.getBlockY() + 2, pos.getBlockZ()))) return false;

        org.bukkit.Location center = centerOf(pos);
        double distanceToBot = bot.getLocation().distance(center);
        double distanceToTarget = target.getLocation().distance(center);
        double maxTargetDistance = Math.max(3.8D, Math.min(maxPlacementDistance - 1.5D, 5.2D));
        if (distanceToBot > maxPlacementDistance || distanceToTarget > maxTargetDistance) return false;
        if (Math.abs(pos.getBlockY() - (target.getLocation().getBlockY() - 1)) > 2) return false;
        return hasLineOfSight(bot.asBukkitPlayer().getEyeLocation().toVector(), center.toVector());
    }

    private double evaluateObsidianScore(BlockVector pos, Player target) {
        org.bukkit.Location center = centerOf(pos);
        double distanceToTarget = center.distance(target.getLocation());
        double distanceToBot = center.distance(bot.getLocation());
        double score = 0.0D;
        score -= Math.abs(distanceToTarget - 2.7D) * 2.8D;
        score -= Math.max(0.0D, distanceToBot - 4.8D) * 2.3D;
        score -= Math.max(0.0D, distanceToTarget - 4.2D) * 3.6D;
        score -= Math.abs((target.getLocation().getBlockY() - pos.getBlockY()) - 1.0D) * 2.0D;
        if (hasLineOfSight(bot.asBukkitPlayer().getEyeLocation().toVector(), center.toVector())) score += 2.1D;
        if (hasLineOfSight(center.toVector(), target.getEyeLocation().toVector())) score += 1.6D;
        return score;
    }

    private boolean hasLineOfSight(Vector start, Vector end) {
        Vector direction = end.clone().subtract(start);
        double distance = direction.length();
        if (distance < 1.0E-6D) return true;
        RayTraceResult result = bot.getWorld().rayTraceBlocks(
                start.toLocation(bot.getWorld()), direction.normalize(), distance, FluidCollisionMode.NEVER, true);
        return result == null || result.getHitBlock() == null;
    }

    private Block blockAt(BlockVector pos) {
        return blockAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
    }

    private Block blockAt(int x, int y, int z) {
        return bot.getWorld().getBlockAt(x, y, z);
    }

    private org.bukkit.Location centerOf(BlockVector pos) {
        return new org.bukkit.Location(bot.getWorld(), pos.getBlockX() + 0.5D, pos.getBlockY() + 0.5D, pos.getBlockZ() + 0.5D);
    }

    private static boolean isPassable(Block block) {
        Material type = block.getType();
        return type.isAir() || block.isPassable();
    }

    private static boolean isSolid(Block block) {
        Material type = block.getType();
        return type.isBlock() && type.isSolid() && !block.isPassable();
    }
}
