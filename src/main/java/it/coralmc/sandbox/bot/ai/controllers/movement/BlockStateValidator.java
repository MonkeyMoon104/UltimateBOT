package it.coralmc.sandbox.bot.ai.controllers.movement;

import it.coralmc.sandbox.bot.ai.controllers.movement.interf.IBlockStateValidator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public class BlockStateValidator implements IBlockStateValidator {
    private static final long CACHE_CLEAN_INTERVAL = 5000;
    private static final int MAX_CACHE_SIZE = 1000;

    private final Level level;
    private final Map<BlockPos, Boolean> blockStateCache = new HashMap<>();
    private long lastCacheClean = 0;

    public BlockStateValidator(Level level) {
        this.level = level;
    }

    @Override
    public boolean isPositionPassableCached(BlockPos pos) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCacheClean > CACHE_CLEAN_INTERVAL) {
            cleanCache();
            lastCacheClean = currentTime;
        }

        Boolean cached = blockStateCache.get(pos);
        if (cached != null) {
            return cached;
        }

        if (blockStateCache.size() >= MAX_CACHE_SIZE) {
            blockStateCache.clear();
        }

        boolean result = isPositionPassable(pos);
        blockStateCache.put(pos, result);
        return result;
    }

    private void cleanCache() {
        if (blockStateCache.size() > MAX_CACHE_SIZE / 2) {
            blockStateCache.clear();
        }
    }

    @Override
    public boolean isPositionPassable(BlockPos pos) {
        try {
            if (pos.getY() < -64 || pos.getY() > 319) {
                return false;
            }

            BlockState current = level.getBlockState(pos);
            BlockState above = level.getBlockState(pos.above());
            BlockState below = level.getBlockState(pos.below());

            boolean canStandOn = !below.isAir();
            boolean canPassThrough = current.isAir() && above.isAir();

            return canPassThrough && canStandOn;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void clearCache() {
        blockStateCache.clear();
        lastCacheClean = System.currentTimeMillis();
    }

    @Override
    public void forceCacheClean() {
        if (blockStateCache.size() > 0) {
            blockStateCache.clear();
            lastCacheClean = System.currentTimeMillis();
        }
    }

    @Override
    public int getCacheSize() {
        return blockStateCache.size();
    }
}