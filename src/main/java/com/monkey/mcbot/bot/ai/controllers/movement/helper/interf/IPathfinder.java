package com.monkey.mcbot.bot.ai.controllers.movement.helper.interf;

import net.minecraft.world.phys.Vec3;

public interface IPathfinder {
    boolean calculatePathTo(Vec3 targetPos);
    boolean followPath();
    boolean hasActivePath();
    void clearPath();
    boolean shouldRecalculatePath();
    Vec3 getCurrentPathPoint();
}