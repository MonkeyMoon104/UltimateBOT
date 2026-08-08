package com.monkey.ultimatebot.bot.ai.controllers.movement.helper.interf;

import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public interface IMovementExecutor {
    void executeDirectMovement(LivingEntity target, double targetDistance);

    void executeStrafeCircle(LivingEntity target, double targetDistance);

    void executeStrafeFigure8(LivingEntity target, double targetDistance);

    void executeEvasiveZigZag(LivingEntity target, double targetDistance);

    void executeTerrainAdaptive(LivingEntity target, double targetDistance);

    void executeRetreatSpiral(LivingEntity target, double targetDistance);

    void executeCrystalSpamMovement(LivingEntity target, double targetDistance);

    void moveToPosition(Vector targetPos);

    void stopMovement();

    void ensureMovement();
}
