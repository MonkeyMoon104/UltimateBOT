package com.monkey.ultimatebot.bot.ai.controllers.brain.helper.inter;

import org.bukkit.entity.LivingEntity;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

public interface IPathfindingManager {
    void checkForStuck(LivingEntity target);

    void attemptPathfindingOrPearl(LivingEntity target);

    boolean hasObstacleBetween(Vector start, Vector end);

    @Nullable Vector calculatePearlTargetAroundPlayer(LivingEntity target);

    boolean isSafeLandingSpot(BlockVector pos);

    void forceUnstuck(LivingEntity target);

    boolean isUsingPathfinding();

    void setUsingPathfinding(boolean using);
}
