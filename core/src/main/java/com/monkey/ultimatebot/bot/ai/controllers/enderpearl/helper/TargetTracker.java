package com.monkey.ultimatebot.bot.ai.controllers.enderpearl.helper;

import com.monkey.ultimatebot.bot.ai.controllers.enderpearl.helper.inter.ITargetTracker;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("NullAway")
public class TargetTracker implements ITargetTracker {

    private @Nullable Vector lastTargetPosition;
    private Vector predictedTargetMovement = new Vector();

    @Override
    public void updateTargetTracking(Player target) {
        Vector currentPos = target.getLocation().toVector();
        if (lastTargetPosition != null) {
            Vector movement = currentPos.clone().subtract(lastTargetPosition);
            predictedTargetMovement = movement.multiply(0.8).add(predictedTargetMovement.multiply(0.2));
        }
        lastTargetPosition = currentPos;
    }

    @Override
    public Vector getPredictedTargetMovement() {
        return predictedTargetMovement;
    }

    @Override
    public @Nullable Vector getLastTargetPosition() {
        return lastTargetPosition;
    }
}
