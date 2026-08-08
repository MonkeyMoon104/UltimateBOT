package com.monkey.ultimatebot.bot.ai.controllers.movement.helper;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.monkey.ultimatebot.bot.ai.controllers.movement.helper.interf.IBlockStateValidator;
import com.monkey.ultimatebot.config.RuntimeSettings;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BlockVector;

public class BlockStateValidator implements IBlockStateValidator {
    private final World world;
    private volatile Cache<BlockVector, Boolean> standablePositionCache;
    private volatile Cache<BlockVector, Boolean> bodySpaceCache;

    public BlockStateValidator(World world, RuntimeSettings.CacheSettings settings) {
        this.world = world;
        this.standablePositionCache = createCache(settings);
        this.bodySpaceCache = createCache(settings);
    }

    public void reconfigure(RuntimeSettings.CacheSettings settings) {
        Cache<BlockVector, Boolean> previousStandableCache = standablePositionCache;
        Cache<BlockVector, Boolean> previousBodySpaceCache = bodySpaceCache;
        standablePositionCache = createCache(settings);
        bodySpaceCache = createCache(settings);
        previousStandableCache.invalidateAll();
        previousBodySpaceCache.invalidateAll();
    }

    private static Cache<BlockVector, Boolean> createCache(RuntimeSettings.CacheSettings settings) {
        return Caffeine.newBuilder()
                .maximumSize(settings.maximumSize())
                .expireAfterWrite(settings.expireAfterWrite())
                .recordStats()
                .build();
    }

    @Override
    public boolean isPositionPassableCached(BlockVector pos) {
        if (containsCobweb(pos)) {
            return false;
        }
        return standablePositionCache.get(cacheKey(pos), this::isPositionPassable);
    }

    @Override
    public boolean isPositionPassable(BlockVector pos) {
        try {
            if (pos.getBlockY() < world.getMinHeight() || pos.getBlockY() > world.getMaxHeight() - 1) {
                return false;
            }

            Block below = blockAt(below(pos));
            return isBodySpaceClearCached(pos) && below.getType() != Material.COBWEB && below.getType().isSolid();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isBodySpaceClearCached(BlockVector pos) {
        if (containsCobweb(pos)) {
            return false;
        }
        return bodySpaceCache.get(cacheKey(pos), this::isBodySpaceClear);
    }

    @Override
    public boolean isBodySpaceClear(BlockVector pos) {
        try {
            if (pos.getBlockY() < world.getMinHeight() || pos.getBlockY() > world.getMaxHeight() - 2) {
                return false;
            }

            Block feet = blockAt(pos);
            Block head = blockAt(above(pos));
            return feet.getType() != Material.COBWEB
                    && head.getType() != Material.COBWEB
                    && feet.isPassable()
                    && head.isPassable()
                    && !feet.isLiquid()
                    && !head.isLiquid();
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

    private boolean containsCobweb(BlockVector position) {
        return blockAt(position).getType() == Material.COBWEB
                || blockAt(above(position)).getType() == Material.COBWEB
                || blockAt(below(position)).getType() == Material.COBWEB;
    }

    private Block blockAt(BlockVector position) {
        return world.getBlockAt(position.getBlockX(), position.getBlockY(), position.getBlockZ());
    }

    private static BlockVector above(BlockVector position) {
        return new BlockVector(position.getBlockX(), position.getBlockY() + 1, position.getBlockZ());
    }

    private static BlockVector below(BlockVector position) {
        return new BlockVector(position.getBlockX(), position.getBlockY() - 1, position.getBlockZ());
    }

    private static BlockVector cacheKey(BlockVector position) {
        return new BlockVector(position.getBlockX(), position.getBlockY(), position.getBlockZ());
    }
}
