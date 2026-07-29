package com.monkey.mcbot.bot.ai.controllers.movement.helper;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.monkey.mcbot.bot.ai.controllers.movement.helper.interf.IBlockStateValidator;
import com.monkey.mcbot.config.RuntimeSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class BlockStateValidator implements IBlockStateValidator {
    private final Level level;
    private volatile Cache<BlockPos, Boolean> blockStateCache;

    public BlockStateValidator(Level level, RuntimeSettings.CacheSettings settings) {
        this.level = level;
        this.blockStateCache = createCache(settings);
    }

    public void reconfigure(RuntimeSettings.CacheSettings settings) {
        Cache<BlockPos, Boolean> previousCache = blockStateCache;
        blockStateCache = createCache(settings);
        previousCache.invalidateAll();
    }

    private static Cache<BlockPos, Boolean> createCache(RuntimeSettings.CacheSettings settings) {
        return Caffeine.newBuilder()
                .maximumSize(settings.maximumSize())
                .expireAfterWrite(settings.expireAfterWrite())
                .recordStats()
                .build();
    }

    @Override
    public boolean isPositionPassableCached(BlockPos pos) {
        return blockStateCache.get(pos.immutable(), this::isPositionPassable);
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
        blockStateCache.invalidateAll();
    }

    @Override
    public void forceCacheClean() {
        blockStateCache.cleanUp();
    }

    @Override
    public int getCacheSize() {
        return Math.toIntExact(blockStateCache.estimatedSize());
    }
}
