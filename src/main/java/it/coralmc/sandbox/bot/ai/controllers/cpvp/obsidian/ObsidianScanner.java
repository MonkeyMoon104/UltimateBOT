package it.coralmc.sandbox.bot.ai.controllers.cpvp;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

public class ObsidianScanner {

    private final Level level;
    private static final long OBSIDIAN_CACHE_MS = 7000;

    public ObsidianScanner(Level level) {
        this.level = level;
    }

    public void scanForExistingObsidian(Player target, Map<BlockPos, Long> obsidianCache) {
        BlockPos targetPos = target.blockPosition();
        long currentTime = System.currentTimeMillis();

        for (int y = -5; y <= 4; y++) {
            for (int x = -9; x <= 9; x++) {
                for (int z = -9; z <= 9; z++) {
                    BlockPos checkPos = targetPos.offset(x, y, z);

                    if (obsidianCache.containsKey(checkPos) &&
                            currentTime - obsidianCache.get(checkPos) < OBSIDIAN_CACHE_MS) {
                        continue;
                    }

                    BlockState state = level.getBlockState(checkPos);
                    if (state.is(Blocks.OBSIDIAN) || state.is(Blocks.BEDROCK)) {
                        obsidianCache.put(checkPos, currentTime);
                    }
                }
            }
        }
    }
}