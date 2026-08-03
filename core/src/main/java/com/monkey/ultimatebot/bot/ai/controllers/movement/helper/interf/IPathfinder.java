package com.monkey.ultimatebot.bot.ai.controllers.movement.helper.interf;

import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public interface IPathfinder {
    boolean calculatePathTo(Vec3 targetPos);

    boolean followPath();

    boolean hasActivePath();

    void clearPath();

    boolean shouldRecalculatePath();

    boolean shouldRecalculatePath(Vec3 targetPos);

    boolean isPathObstructed(Vec3 targetPos);

    @Nullable Vec3 getCurrentPathPoint();
}
