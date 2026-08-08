package com.monkey.ultimatebot.bot.ai.controllers.movement.helper.interf;

import org.bukkit.util.BlockVector;

public interface IBlockStateValidator {
    boolean isPositionPassable(BlockVector pos);

    boolean isPositionPassableCached(BlockVector pos);

    boolean isBodySpaceClear(BlockVector pos);

    boolean isBodySpaceClearCached(BlockVector pos);

    void clearCache();

    void forceCacheClean();

    int getCacheSize();
}
