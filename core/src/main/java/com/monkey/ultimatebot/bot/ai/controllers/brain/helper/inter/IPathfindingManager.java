package com.monkey.ultimatebot.bot.ai.controllers.brain.helper.inter;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public interface IPathfindingManager {
    void checkForStuck(LivingEntity target);

    void attemptPathfindingOrPearl(LivingEntity target);

    boolean hasObstacleBetween(Vec3 start, Vec3 end);

    Vec3 calculatePearlTargetAroundPlayer(LivingEntity target);

    boolean isSafeLandingSpot(BlockPos pos);

    void forceUnstuck(LivingEntity target);

    boolean isUsingPathfinding();

    void setUsingPathfinding(boolean using);
}
