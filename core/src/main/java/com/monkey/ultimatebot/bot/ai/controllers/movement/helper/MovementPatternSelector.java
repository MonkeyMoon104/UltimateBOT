package com.monkey.ultimatebot.bot.ai.controllers.movement.helper;

import com.monkey.ultimatebot.bot.ai.controllers.movement.helper.interf.IMovementPatternSelector;
import com.monkey.ultimatebot.bot.ai.controllers.movement.helper.interf.IObstacleHandler;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class MovementPatternSelector implements IMovementPatternSelector {
    private static final long MIN_PATTERN_DURATION = 1500;

    private final IObstacleHandler obstacleHandler;
    private MovementPattern currentPattern = MovementPattern.DIRECT;
    private long lastPatternChange = 0;

    public MovementPatternSelector(IObstacleHandler obstacleHandler) {
        this.obstacleHandler = obstacleHandler;
    }

    @Override
    public MovementPattern selectOptimalPattern(
            LivingEntity target,
            double targetDistance,
            Vector botPos,
            Vector targetPos,
            boolean isUnderFire,
            int consecutiveHits) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastPatternChange < MIN_PATTERN_DURATION) {
            return currentPattern;
        }

        double distance = botPos.distance(targetPos);
        double yDiff = botPos.getY() - targetPos.getY();
        boolean airborneTargetAbove = !target.isOnGround() && targetPos.getY() > botPos.getY() + 0.5D;

        MovementPattern newPattern;
        if (airborneTargetAbove && distance <= 8.0D) {
            newPattern = distance < 3.5D ? MovementPattern.STRAFE_CIRCLE : MovementPattern.DIRECT;
        } else if (isUnderFire || consecutiveHits >= 2) {
            newPattern = MovementPattern.EVASIVE_ZIG_ZAG;
        } else if (distance < 4.0 && targetDistance < 4.0) {
            newPattern = MovementPattern.STRAFE_CIRCLE;
        } else if (distance >= 4.0 && distance <= 8.0) {
            if (obstacleHandler.hasComplexTerrain(botPos)) {
                newPattern = MovementPattern.TERRAIN_ADAPTIVE;
            } else {
                newPattern = Math.random() < 0.6 ? MovementPattern.STRAFE_FIGURE8 : MovementPattern.STRAFE_CIRCLE;
            }
        } else if (distance > 8.0) {
            if (yDiff < -2.0 || obstacleHandler.hasObstacles(botPos, targetPos)) {
                newPattern = MovementPattern.TERRAIN_ADAPTIVE;
            } else {
                newPattern = MovementPattern.DIRECT;
            }
        } else {
            newPattern = MovementPattern.DIRECT;
        }

        setMovementPattern(newPattern);
        return newPattern;
    }

    @Override
    public void setMovementPattern(MovementPattern pattern) {
        if (pattern != currentPattern) {
            currentPattern = pattern;
            lastPatternChange = System.currentTimeMillis();
        }
    }

    @Override
    public MovementPattern getCurrentPattern() {
        return currentPattern;
    }

    @Override
    public boolean shouldChangePattern() {
        return System.currentTimeMillis() - lastPatternChange > MIN_PATTERN_DURATION;
    }
}
