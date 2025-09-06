package it.coralmc.sandbox.bot.ai.controllers.enderpearl;

import it.coralmc.sandbox.bot.ai.controllers.enderpearl.inter.ITargetTracker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class TargetTracker implements ITargetTracker {

    private Vec3 lastTargetPosition;
    private Vec3 predictedTargetMovement = Vec3.ZERO;

    @Override
    public void updateTargetTracking(Player target) {
        Vec3 currentPos = target.position();
        if (lastTargetPosition != null) {
            Vec3 movement = currentPos.subtract(lastTargetPosition);
            predictedTargetMovement = movement.scale(0.8).add(predictedTargetMovement.scale(0.2));
        }
        lastTargetPosition = currentPos;
    }

    @Override
    public Vec3 getPredictedTargetMovement() {
        return predictedTargetMovement;
    }

    @Override
    public Vec3 getLastTargetPosition() {
        return lastTargetPosition;
    }
}