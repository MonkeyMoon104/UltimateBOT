package com.monkey.ultimatebot.bot.ai.controllers.movement.pathfinding;

import com.monkey.ultimatebot.bot.ai.controllers.movement.helper.interf.IBlockStateValidator;
import java.util.Objects;
import org.bukkit.util.BlockVector;

public final class UltimateBotTraversalEnvironment implements BotTraversalEnvironment {
    private final IBlockStateValidator blockValidator;

    public UltimateBotTraversalEnvironment(IBlockStateValidator blockValidator) {
        this.blockValidator = Objects.requireNonNull(blockValidator, "blockValidator");
    }

    @Override
    public boolean canStandAt(BlockVector position) {
        return blockValidator.isPositionPassableCached(position);
    }

    @Override
    public boolean canOccupy(BlockVector position) {
        return blockValidator.isBodySpaceClearCached(position);
    }
}
