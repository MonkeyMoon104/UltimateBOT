package com.monkey.ultimatebot.bot.ai.controllers.rapvp.helper;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.controllers.combat.ExplosionDamageEstimator;
import com.monkey.ultimatebot.bot.ai.difficulty.DifficultyLevel;
import com.monkey.ultimatebot.bot.ai.difficulty.DifficultyProfileFactory;
import com.monkey.ultimatebot.bot.ai.difficulty.configs.RAPVPConfig;
import com.monkey.ultimatebot.access.block.BlockPassableAccess;
import com.monkey.ultimatebot.access.item.MaterialAirAccess;
import java.util.Optional;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

@SuppressWarnings("NullAway")
public class AnchorPositionFinder {

    private final ITrainingBot bot;
    private long lastSearchTime = 0;
    private RAPVPConfig config = DifficultyProfileFactory.buildRAPVPConfig(DifficultyLevel.NORMAL);

    public AnchorPositionFinder(ITrainingBot bot) {
        this.bot = bot;
    }

    public Optional<BlockVector> findBestAnchorPos(Player target) {
        if (config == null) return Optional.empty();
        long now = System.currentTimeMillis();
        if (now - lastSearchTime < config.getAnchorSearchCooldownMillis()) return Optional.empty();
        lastSearchTime = now;

        Vector targetPos = target.getLocation().toVector();
        BlockVector best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        double maxDistanceSq = (double) config.getMaxDistance() * config.getMaxDistance();
        int horizontalRadius = Math.max(3, Math.min(config.getMaxDistance(), 8));
        int downY = -Math.max(2, horizontalRadius / 2);
        int upY = Math.max(1, horizontalRadius / 3);

        for (int dx = -horizontalRadius; dx <= horizontalRadius; dx++) {
            for (int dz = -horizontalRadius; dz <= horizontalRadius; dz++) {
                for (int dy = downY; dy <= upY; dy++) {
                    BlockVector check = new BlockVector(
                            targetPos.getBlockX() + dx, targetPos.getBlockY() + dy, targetPos.getBlockZ() + dz);
                    if (isBotBlock(check)
                            || isBotBlock(new BlockVector(check.getBlockX(), check.getBlockY() + 1, check.getBlockZ())))
                        continue;
                    Block block = blockAt(check);
                    Block below = block.getRelative(0, -1, 0);
                    if (!isPassable(block) || !isSolid(below)) continue;
                    org.bukkit.Location anchorCenter = centerOf(check);
                    if (bot.getLocation().distanceSquared(anchorCenter) > maxDistanceSq) continue;
                    if (anchorCenter.distance(target.getLocation()) > 4.4D) continue;
                    double targetDamage = ExplosionDamageEstimator.estimateAnchorDamage(anchorCenter, target);
                    double selfDamage =
                            ExplosionDamageEstimator.estimateAnchorDamage(anchorCenter, bot.asBukkitPlayer());
                    if (targetDamage < 1.0D || selfDamage >= bot.healthValue() - 1.0D) continue;
                    double score = (targetDamage * 3.4D)
                            - (selfDamage * 2.9D)
                            - bot.getLocation().distance(anchorCenter) * 0.45D;
                    if (score > bestScore) {
                        bestScore = score;
                        best = check;
                    }
                }
            }
        }
        return Optional.ofNullable(best);
    }

    public void setConfig(RAPVPConfig config) {
        this.config = config;
    }

    private boolean isBotBlock(BlockVector vector) {
        return vector.getBlockX() == bot.getLocation().getBlockX()
                && vector.getBlockY() == bot.getLocation().getBlockY()
                && vector.getBlockZ() == bot.getLocation().getBlockZ();
    }

    private Block blockAt(BlockVector pos) {
        return bot.getWorld().getBlockAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
    }

    private org.bukkit.Location centerOf(BlockVector pos) {
        return new org.bukkit.Location(
                bot.getWorld(), pos.getBlockX() + 0.5D, pos.getBlockY() + 0.5D, pos.getBlockZ() + 0.5D);
    }

    private static boolean isPassable(Block block) {
        Material type = block.getType();
        return MaterialAirAccess.isAir(type) || BlockPassableAccess.isPassable(block);
    }

    private static boolean isSolid(Block block) {
        Material type = block.getType();
        return type.isBlock() && type.isSolid() && !BlockPassableAccess.isPassable(block);
    }
}
