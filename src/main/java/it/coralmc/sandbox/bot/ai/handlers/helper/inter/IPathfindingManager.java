package it.coralmc.sandbox.bot.ai.handlers.helper.inter;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public interface IPathfindingManager {
    void checkForStuck(Player target);
    void attemptPathfindingOrPearl(Player target);
    boolean hasObstacleBetween(Vec3 start, Vec3 end);
    Vec3 calculatePearlTargetAroundPlayer(Player target);
    boolean isSafeLandingSpot(BlockPos pos);
    void forceUnstuck(Player target);
    boolean isUsingPathfinding();
    void setUsingPathfinding(boolean using);
}