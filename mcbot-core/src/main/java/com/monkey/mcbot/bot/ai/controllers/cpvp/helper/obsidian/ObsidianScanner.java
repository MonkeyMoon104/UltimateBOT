package com.monkey.mcbot.bot.ai.controllers.cpvp.helper.obsidian;

import com.monkey.mcbot.bot.ai.rank.configs.CPVPConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

public class ObsidianScanner {

    private final Level level;
    private CPVPConfig config;

    public ObsidianScanner(Level level) {
        this.level = level;
    }

    public void scanForExistingObsidian(Player target, Map<BlockPos, Long> obsidianCache) {
        BlockPos targetPos = target.blockPosition();
        long currentTime = System.currentTimeMillis();
        double maxUsefulDistance = Math.max(4.5D, Math.min(config.getMaxCrystalDistance(), 7.0D));

        for (int y = -3; y <= 2; y++) {
            for (int x = -6; x <= 6; x++) {
                for (int z = -6; z <= 6; z++) {
                    BlockPos checkPos = targetPos.offset(x, y, z);

                    if (obsidianCache.containsKey(checkPos) &&
                            currentTime - obsidianCache.get(checkPos) < config.getObsidianCacheMs()) {
                        continue;
                    }

                    int obsidianY = checkPos.getY();
                    int targetY = target.blockPosition().getY();

                    if (obsidianY >= targetY) continue;

                    BlockState state = level.getBlockState(checkPos);
                    if (state.getBlock() == Blocks.OBSIDIAN || state.getBlock() == Blocks.BEDROCK) {
                        if (!level.getBlockState(checkPos.above()).isAir() || !level.getBlockState(checkPos.above(2)).isAir()) {
                            continue;
                        }
                        double distanceToTarget = target.position().distanceTo(net.minecraft.world.phys.Vec3.atCenterOf(checkPos));
                        if (distanceToTarget > maxUsefulDistance) {
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
