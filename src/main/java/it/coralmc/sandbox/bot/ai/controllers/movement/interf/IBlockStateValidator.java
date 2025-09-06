package it.coralmc.sandbox.bot.ai.controllers.movement.interf;

import net.minecraft.core.BlockPos;

public interface IBlockStateValidator {
    boolean isPositionPassable(BlockPos pos);
    boolean isPositionPassableCached(BlockPos pos);
    void clearCache();
    void forceCacheClean();
    int getCacheSize();
}