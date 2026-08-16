package com.monkey.ultimatebot.bot.ai.controllers.cpvp.helper.obsidian;

import com.monkey.ultimatebot.compat.BlockPassableAccess;
import com.monkey.ultimatebot.bot.ai.difficulty.DifficultyLevel;
import com.monkey.ultimatebot.bot.ai.difficulty.DifficultyProfileFactory;
import com.monkey.ultimatebot.bot.ai.difficulty.configs.CPVPConfig;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;

@SuppressWarnings("NullAway")
public class ObsidianScanner {

    private final World world;
    private CPVPConfig config = DifficultyProfileFactory.buildCPVPConfig(DifficultyLevel.NORMAL);

    public ObsidianScanner(World world) {
        this.world = world;
    }

    public void scanForExistingObsidian(Player target, Map<BlockVector, Long> obsidianCache) {
        int targetX = target.getLocation().getBlockX();
        int targetY = target.getLocation().getBlockY();
        int targetZ = target.getLocation().getBlockZ();
        long currentTime = System.currentTimeMillis();
        double maxUsefulDistance = Math.max(4.5D, Math.min(config.getMaxCrystalDistance(), 7.0D));

        for (int y = -3; y <= 2; y++) {
            for (int x = -6; x <= 6; x++) {
                for (int z = -6; z <= 6; z++) {
                    BlockVector checkPos = new BlockVector(targetX + x, targetY + y, targetZ + z);
                    if (obsidianCache.containsKey(checkPos)
                            && currentTime - obsidianCache.get(checkPos) < config.getObsidianCacheMs()) {
                        continue;
                    }
                    if (checkPos.getBlockY() >= targetY) continue;
                    Material type = world.getBlockAt(checkPos.getBlockX(), checkPos.getBlockY(), checkPos.getBlockZ()).getType();
                    if (type == Material.OBSIDIAN || type == Material.BEDROCK) {
                        if (!BlockPassableAccess.isPassable(world.getBlockAt(checkPos.getBlockX(), checkPos.getBlockY() + 1, checkPos.getBlockZ()))
                                || !BlockPassableAccess.isPassable(world.getBlockAt(checkPos.getBlockX(), checkPos.getBlockY() + 2, checkPos.getBlockZ()))) {
                            continue;
                        }
                        org.bukkit.Location center = new org.bukkit.Location(world, checkPos.getBlockX() + 0.5D, checkPos.getBlockY() + 0.5D, checkPos.getBlockZ() + 0.5D);
                        if (target.getLocation().distance(center) > maxUsefulDistance) {
                            continue;
                        }
                        obsidianCache.put(checkPos, currentTime);
                    }
                }
            }
        }
    }

    public void setConfig(CPVPConfig config) {
        this.config = config;
    }
}
