package com.monkey.ultimatebot.bot.ai.controllers.movement.pathfinding;

import com.monkey.ultimatebot.bot.ai.controllers.movement.helper.interf.IBlockStateValidator;
import java.util.Objects;
import net.minecraft.core.BlockPos;

public final class UltimateBotTraversalEnvironment implements BotTraversalEnvironment {
    private final IBlockStateValidator blockValidator;

    public UltimateBotTraversalEnvironment(IBlockStateValidator blockValidator) {
        this.blockValidator = Objects.requireNonNull(blockValidator, "blockValidator");
    }

    @Override
    public boolean canStandAt(BlockPos position) {
        return blockValidator.isPositionPassableCached(position);
    }

    @Override
    public boolean canOccupy(BlockPos position) {
        return blockValidator.isBodySpaceClearCached(position);
    }
}
