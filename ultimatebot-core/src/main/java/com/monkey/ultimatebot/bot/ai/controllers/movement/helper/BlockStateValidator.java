package com.monkey.ultimatebot.bot.ai.controllers.movement.helper;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.monkey.ultimatebot.bot.ai.controllers.movement.helper.interf.IBlockStateValidator;
import com.monkey.ultimatebot.config.RuntimeSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class BlockStateValidator implements IBlockStateValidator {
    private final Level level;
    private volatile Cache<BlockPos, Boolean> standablePositionCache;
    private volatile Cache<BlockPos, Boolean> bodySpaceCache;

    public BlockStateValidator(Level level, RuntimeSettings.CacheSettings settings) {
        this.level = level;
        this.standablePositionCache = createCache(settings);
        this.bodySpaceCache = createCache(settings);
    }

    public void reconfigure(RuntimeSettings.CacheSettings settings) {
        Cache<BlockPos, Boolean> previousStandableCache = standablePositionCache;
        Cache<BlockPos, Boolean> previousBodySpaceCache = bodySpaceCache;
        standablePositionCache = createCache(settings);
        bodySpaceCache = createCache(settings);
        previousStandableCache.invalidateAll();
        previousBodySpaceCache.invalidateAll();
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
        return standablePositionCache.get(pos.immutable(), this::isPositionPassable);
    }

    @Override
    public boolean isPositionPassable(BlockPos pos) {
        try {
            if (pos.getY() < -64 || pos.getY() > 319) {
                return false;
            }

            BlockState below = level.getBlockState(pos.below());
            return isBodySpaceClearCached(pos)
                    && !below.getCollisionShape(level, pos.below()).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isBodySpaceClearCached(BlockPos pos) {
        return bodySpaceCache.get(pos.immutable(), this::isBodySpaceClear);
    }

    @Override
    public boolean isBodySpaceClear(BlockPos pos) {
        try {
            if (pos.getY() < -64 || pos.getY() > 318) {
                return false;
            }

            BlockState feet = level.getBlockState(pos);
            BlockState head = level.getBlockState(pos.above());
            return feet.getCollisionShape(level, pos).isEmpty()
                    && head.getCollisionShape(level, pos.above()).isEmpty()
                    && feet.getFluidState().isEmpty()
                    && head.getFluidState().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void clearCache() {
        standablePositionCache.invalidateAll();
        bodySpaceCache.invalidateAll();
    }

    @Override
    public void forceCacheClean() {
        standablePositionCache.cleanUp();
        bodySpaceCache.cleanUp();
    }

    @Override
    public int getCacheSize() {
        return Math.toIntExact(standablePositionCache.estimatedSize() + bodySpaceCache.estimatedSize());
    }
}
