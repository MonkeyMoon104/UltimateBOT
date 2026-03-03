package com.monkey.mcbot.bot.ai.controllers.movement.helper.interf;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public interface IMovementExecutor {
    void executeDirectMovement(Player target, double targetDistance);
    void executeStrafeCircle(Player target, double targetDistance);
    void executeStrafeFigure8(Player target, double targetDistance);
    void executeEvasiveZigZag(Player target, double targetDistance);
    void executeTerrainAdaptive(Player target, double targetDistance);
    void executeRetreatSpiral(Player target, double targetDistance);
    void executeCrystalSpamMovement(Player target, double targetDistance);
    void moveToPosition(Vec3 targetPos);
    void stopMovement();
    void ensureMovement();
}