package com.monkey.ultimatebot.bot.ai.controllers.cpvp.helper.obsidian;

import com.monkey.ultimatebot.bot.ai.difficulty.DifficultyLevel;
import com.monkey.ultimatebot.bot.ai.difficulty.DifficultyProfileFactory;
import com.monkey.ultimatebot.bot.ai.difficulty.configs.CPVPConfig;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ObsidianScanner {

    private final Level level;
    private CPVPConfig config = DifficultyProfileFactory.buildCPVPConfig(DifficultyLevel.NORMAL);

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

                    if (obsidianCache.containsKey(checkPos)
                            && currentTime - obsidianCache.get(checkPos) < config.getObsidianCacheMs()) {
                        continue;
                    }

                    int obsidianY = checkPos.getY();
                    int targetY = target.blockPosition().getY();

                    if (obsidianY >= targetY) continue;

                    BlockState state = level.getBlockState(checkPos);
                    if (Blocks.OBSIDIAN.equals(state.getBlock()) || Blocks.BEDROCK.equals(state.getBlock())) {
                        if (!level.getBlockState(checkPos.above()).isAir()
                                || !level.getBlockState(checkPos.above(2)).isAir()) {
                            continue;
                        }
                        double distanceToTarget =
                                target.position().distanceTo(net.minecraft.world.phys.Vec3.atCenterOf(checkPos));
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
