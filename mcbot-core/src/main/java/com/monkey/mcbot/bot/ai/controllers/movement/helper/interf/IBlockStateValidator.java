package com.monkey.mcbot.bot.ai.controllers.movement.helper.interf;

import net.minecraft.core.BlockPos;

public interface IBlockStateValidator {
    boolean isPositionPassable(BlockPos pos);

    boolean isPositionPassableCached(BlockPos pos);

    boolean isBodySpaceClear(BlockPos pos);

    boolean isBodySpaceClearCached(BlockPos pos);

    void clearCache();

    void forceCacheClean();

    int getCacheSize();
}
