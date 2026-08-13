package com.monkey.ultimatebot.bot.ai.controllers.movement.helper;

import com.monkey.ultimatebot.bot.ai.controllers.movement.helper.interf.IBlockStateValidator;
import com.monkey.ultimatebot.config.RuntimeSettings;
import com.monkey.ultimatebot.compat.WorldAccess;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BlockVector;

/**
 * Block walkability checks for pathfinding. Uses a single-threaded scratch map instead of Caffeine:
 * A* hits this thousands of times per search with mostly unique keys, so ConcurrentHashMap +
 * expireAfterWrite + afterWrite drains dominated the server thread (see Spark
 * LocalManualCache.get / BoundedLocalCache.computeIfAbsent).
 */
public class BlockStateValidator implements IBlockStateValidator {
    private final World world;
    private final Map<Long, Boolean> standablePositionCache = new HashMap<>(4_096);
    private final Map<Long, Boolean> bodySpaceCache = new HashMap<>(4_096);
    private int maximumSize;

    public BlockStateValidator(World world, RuntimeSettings.CacheSettings settings) {
        this.world = world;
        this.maximumSize = clampSize(settings.maximumSize());
    }

    public void reconfigure(RuntimeSettings.CacheSettings settings) {
        this.maximumSize = clampSize(settings.maximumSize());
        clearCache();
    }

    @Override
    public boolean isPositionPassableCached(BlockVector pos) {
        long key = pack(pos);
        Boolean cached = standablePositionCache.get(key);
        if (cached != null) {
            return cached;
        }
        boolean value = isPositionPassable(pos);
        remember(standablePositionCache, key, value);
        return value;
    }

    @Override
    public boolean isPositionPassable(BlockVector pos) {
        try {
            int y = pos.getBlockY();
            if (y < WorldAccess.minHeight(world) || y > WorldAccess.maxHeight(world) - 1) {
                return false;
            }

            Block below = blockAt(pos.getBlockX(), y - 1, pos.getBlockZ());
            Material belowType = below.getType();
            if (belowType == Material.COBWEB || !belowType.isSolid()) {
                return false;
            }
            return isBodySpaceClearCached(pos);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isBodySpaceClearCached(BlockVector pos) {
        long key = pack(pos);
        Boolean cached = bodySpaceCache.get(key);
        if (cached != null) {
            return cached;
        }
        boolean value = isBodySpaceClear(pos);
        remember(bodySpaceCache, key, value);
        return value;
    }

    @Override
    public boolean isBodySpaceClear(BlockVector pos) {
        try {
            int x = pos.getBlockX();
            int y = pos.getBlockY();
            int z = pos.getBlockZ();
            if (y < WorldAccess.minHeight(world) || y > WorldAccess.maxHeight(world) - 2) {
                return false;
            }

            Block feet = blockAt(x, y, z);
            Block head = blockAt(x, y + 1, z);
            Material feetType = feet.getType();
            Material headType = head.getType();
            return feetType != Material.COBWEB
                    && headType != Material.COBWEB
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
        standablePositionCache.clear();
        bodySpaceCache.clear();
    }

    @Override
    public void forceCacheClean() {
        // Scratch maps have no deferred cleanup; clear keeps memory bounded between searches.
        clearCache();
    }

    @Override
    public int getCacheSize() {
        return standablePositionCache.size() + bodySpaceCache.size();
    }

    private void remember(Map<Long, Boolean> cache, long key, boolean value) {
        if (cache.size() >= maximumSize) {
            cache.clear();
        }
        cache.put(key, value);
    }

    private Block blockAt(int x, int y, int z) {
        return world.getBlockAt(x, y, z);
    }

    private static int clampSize(long maximumSize) {
        return (int) Math.min(Math.max(maximumSize, 1_024L), 65_536L);
    }

    private static long pack(BlockVector position) {
        long x = position.getBlockX() & 0x3FFFFFFL;
        long y = position.getBlockY() & 0xFFFL;
        long z = position.getBlockZ() & 0x3FFFFFFL;
        return x << 38 | z << 12 | y;
    }
}
