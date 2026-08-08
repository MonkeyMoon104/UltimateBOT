package com.monkey.ultimatebot.bot.ai.controllers.movement.helper.interf;

import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

public interface IPathfinder {
    boolean calculatePathTo(Vector targetPos);

    boolean followPath();

    boolean hasActivePath();

    void clearPath();

    boolean shouldRecalculatePath();

    boolean shouldRecalculatePath(Vector targetPos);

    boolean isPathObstructed(Vector targetPos);

    @Nullable Vector getCurrentPathPoint();
}
