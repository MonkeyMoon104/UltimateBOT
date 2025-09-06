package it.coralmc.sandbox.bot.ai.controllers.movement.interf;

import it.coralmc.sandbox.bot.ai.controllers.movement.MovementPattern;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public interface IMovementPatternSelector {
    MovementPattern selectOptimalPattern(Player target, double targetDistance, Vec3 botPos, Vec3 targetPos, boolean isUnderFire, int consecutiveHits);
    void setMovementPattern(MovementPattern pattern);
    MovementPattern getCurrentPattern();
    boolean shouldChangePattern();
}