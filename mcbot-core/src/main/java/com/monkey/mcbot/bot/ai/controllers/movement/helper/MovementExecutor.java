package com.monkey.mcbot.bot.ai.controllers.movement.helper;

import com.monkey.mcbot.bot.ai.controllers.movement.helper.interf.IBlockStateValidator;
import com.monkey.mcbot.bot.ai.controllers.movement.helper.interf.IMovementExecutor;
import com.monkey.mcbot.bot.ai.controllers.movement.helper.interf.IObstacleHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class MovementExecutor implements IMovementExecutor {
    private static final int ZIG_ZAG_CHANGE_TICKS = 12;

    private final Player bot;
    private final IBlockStateValidator blockValidator;
    private final IObstacleHandler obstacleHandler;

    private double movementSpeed = 0.25;
    private double jumpVelocity = 0.42;
    private double strafeAngle = 0.0;
    private boolean strafeClockwise = true;
    private int zigZagDirection = 1;
    private int zigZagCounter = 0;
    private final PathSteering pathSteering = new PathSteering();

    public MovementExecutor(Player bot, IBlockStateValidator blockValidator, IObstacleHandler obstacleHandler) {
        this.bot = bot;
        this.blockValidator = blockValidator;
        this.obstacleHandler = obstacleHandler;
    }

    @Override
    public void executeDirectMovement(Player target, double targetDistance) {
        double targetX = target.getX();
        double targetZ = target.getZ();
        double botX = bot.getX();
        double botZ = bot.getZ();

        double dx = targetX - botX;
        double dz = targetZ - botZ;
        double currentDistance = Math.sqrt(dx * dx + dz * dz);

        if (Math.abs(currentDistance - targetDistance) <= 0.3) {
            stopMovement();
            return;
        }

        if (currentDistance == 0) return;

        if (targetDistance > 0) {
            double normalizedDx = dx / currentDistance;
            double normalizedDz = dz / currentDistance;
            double targetPointX = targetX - (normalizedDx * targetDistance);
            double targetPointZ = targetZ - (normalizedDz * targetDistance);

            dx = targetPointX - botX;
            dz = targetPointZ - botZ;

            double distanceToTargetPoint = Math.sqrt(dx * dx + dz * dz);
            if (distanceToTargetPoint < 0.1) {
                stopMovement();
                return;
            }
        }

        double length = Math.sqrt(dx * dx + dz * dz);
        if (length == 0) return;

        dx = dx / length;
        dz = dz / length;

        double moveX = dx * movementSpeed;
        double moveZ = dz * movementSpeed;

        if (obstacleHandler.handleObstacles(dx, dz, botX, bot.getY(), botZ, moveX, moveZ)) {
            return;
        }

        bot.setDeltaMovement(moveX, bot.getDeltaMovement().y, moveZ);
    }

    @Override
    public void executeStrafeCircle(Player target, double targetDistance) {
        Vec3 targetPos = target.position();
        Vec3 botPos = bot.position();
        double angleSpeed = 0.08 + (Math.random() * 0.04);
        strafeAngle += strafeClockwise ? angleSpeed : -angleSpeed;

        double desiredX = targetPos.x + Math.cos(strafeAngle) * targetDistance;
        double desiredZ = targetPos.z + Math.sin(strafeAngle) * targetDistance;
        Vec3 desiredPos = new Vec3(desiredX, botPos.y, desiredZ);

        Vec3 direction = desiredPos.subtract(botPos).normalize();
        double moveX = direction.x * movementSpeed * 1.1;
        double moveZ = direction.z * movementSpeed * 1.1;

        if (Math.random() < 0.01) {
            strafeClockwise = !strafeClockwise;
        }

        if (!obstacleHandler.handleObstacles(direction.x, direction.z, botPos.x, botPos.y, botPos.z, moveX, moveZ)) {
            bot.setDeltaMovement(moveX, bot.getDeltaMovement().y, moveZ);
        }
    }

    @Override
    public void executeStrafeFigure8(Player target, double targetDistance) {
        Vec3 targetPos = target.position();
        Vec3 botPos = bot.position();

        strafeAngle += 0.12;
        double radiusX = targetDistance * 0.8;
        double radiusZ = targetDistance * 1.2;

        double desiredX = targetPos.x + Math.cos(strafeAngle) * radiusX;
        double desiredZ = targetPos.z + Math.sin(strafeAngle * 2) * radiusZ;

        Vec3 desiredPos = new Vec3(desiredX, botPos.y, desiredZ);
        Vec3 direction = desiredPos.subtract(botPos).normalize();

        double moveX = direction.x * movementSpeed * 1.05;
        double moveZ = direction.z * movementSpeed * 1.05;

        if (!obstacleHandler.handleObstacles(direction.x, direction.z, botPos.x, botPos.y, botPos.z, moveX, moveZ)) {
            bot.setDeltaMovement(moveX, bot.getDeltaMovement().y, moveZ);
        }
    }

    @Override
    public void executeEvasiveZigZag(Player target, double targetDistance) {
        Vec3 targetPos = target.position();
        Vec3 botPos = bot.position();

        zigZagCounter++;
        if (zigZagCounter >= ZIG_ZAG_CHANGE_TICKS) {
            zigZagDirection *= -1;
            zigZagCounter = 0;
            if (Math.random() < 0.3) {
                zigZagDirection *= Math.random() < 0.5 ? 1 : -1;
            }
        }

        Vec3 toTarget = targetPos.subtract(botPos);
        double distanceToTarget = toTarget.length();
        if (distanceToTarget == 0) return;

        Vec3 baseDirection = toTarget.normalize();

        if (distanceToTarget > targetDistance) {
            Vec3 perpendicular = new Vec3(-baseDirection.z, 0, baseDirection.x);
            Vec3 zigzagDirection = baseDirection.add(perpendicular.scale(zigZagDirection * 0.7));
            zigzagDirection = zigzagDirection.normalize();

            double moveX = zigzagDirection.x * movementSpeed * 1.2;
            double moveZ = zigzagDirection.z * movementSpeed * 1.2;

            if (!obstacleHandler.handleObstacles(
                    zigzagDirection.x, zigzagDirection.z, botPos.x, botPos.y, botPos.z, moveX, moveZ)) {
                bot.setDeltaMovement(moveX, bot.getDeltaMovement().y, moveZ);
            }
        } else {
            Vec3 perpendicular = new Vec3(-baseDirection.z, 0, baseDirection.x);
            Vec3 strafeDir = perpendicular.scale(zigZagDirection);

            double moveX = strafeDir.x * movementSpeed;
            double moveZ = strafeDir.z * movementSpeed;

            if (!obstacleHandler.handleObstacles(
                    strafeDir.x, strafeDir.z, botPos.x, botPos.y, botPos.z, moveX, moveZ)) {
                bot.setDeltaMovement(moveX, bot.getDeltaMovement().y, moveZ);
            }
        }
    }

    @Override
    public void executeTerrainAdaptive(Player target, double targetDistance) {
        Vec3 targetPos = target.position();
        Vec3 botPos = bot.position();

        Vec3 bestDirection = findBestPath(botPos, targetPos, targetDistance);
        if (bestDirection != null) {
            double moveX = bestDirection.x * movementSpeed;
            double moveZ = bestDirection.z * movementSpeed;

            if (!obstacleHandler.handleObstacles(
                    bestDirection.x, bestDirection.z, botPos.x, botPos.y, botPos.z, moveX, moveZ)) {
                bot.setDeltaMovement(moveX, bot.getDeltaMovement().y, moveZ);
            }
        } else {
            executeDirectMovement(target, targetDistance);
        }
    }

    @Override
    public void executeRetreatSpiral(Player target, double targetDistance) {
        Vec3 targetPos = target.position();
        Vec3 botPos = bot.position();

        strafeAngle += strafeClockwise ? 0.15 : -0.15;

        double currentDistance = botPos.distanceTo(targetPos);
        double spiralRadius = Math.max(targetDistance, currentDistance + 1.0);
        spiralRadius += strafeAngle * 0.1;

        double desiredX = targetPos.x + Math.cos(strafeAngle) * spiralRadius;
        double desiredZ = targetPos.z + Math.sin(strafeAngle) * spiralRadius;

        Vec3 desiredPos = new Vec3(desiredX, botPos.y, desiredZ);
        Vec3 direction = desiredPos.subtract(botPos).normalize();

        double moveX = direction.x * movementSpeed * 1.15;
        double moveZ = direction.z * movementSpeed * 1.15;

        if (!obstacleHandler.handleObstacles(direction.x, direction.z, botPos.x, botPos.y, botPos.z, moveX, moveZ)) {
            bot.setDeltaMovement(moveX, bot.getDeltaMovement().y, moveZ);
        }
    }

    @Override
    public void executeCrystalSpamMovement(Player target, double targetDistance) {
        Vec3 targetPos = target.position();
        Vec3 botPos = bot.position();
        double yDiff = botPos.y - targetPos.y;

        if (yDiff > -2.0) {
            Vec3 belowTarget = new Vec3(targetPos.x, targetPos.y - 3, targetPos.z);
            Vec3 direction = belowTarget.subtract(botPos).normalize();
            double moveX = direction.x * movementSpeed * 1.3;
            double moveZ = direction.z * movementSpeed * 1.3;
            bot.setDeltaMovement(moveX, bot.getDeltaMovement().y, moveZ);
        } else {
            executeStrafeCircle(target, targetDistance);
        }
    }

    @Override
    public void moveToPosition(Vec3 targetPos) {
        Vec3 botPos = bot.position();
        double dx = targetPos.x - botPos.x;
        double dz = targetPos.z - botPos.z;
        double distance = Math.sqrt(dx * dx + dz * dz);

        if (distance < 0.1) {
            stopMovement();
            return;
        }

        dx = dx / distance;
        dz = dz / distance;

        double moveX = dx * movementSpeed;
        double moveZ = dz * movementSpeed;

        if (obstacleHandler.handleObstacles(dx, dz, botPos.x, botPos.y, botPos.z, moveX, moveZ)) {
            return;
        }

        bot.setDeltaMovement(moveX, bot.getDeltaMovement().y, moveZ);

        if (bot.onGround() && bot.getDeltaMovement().horizontalDistance() < 0.1) {
            ensureMovement();
        }
    }

    public void moveAlongPath(Vec3 targetPos) {
        Vec3 botPos = bot.position();
        Vec3 delta = targetPos.subtract(botPos);
        double horizontalDistance = delta.horizontalDistance();
        if (horizontalDistance < 0.05D) {
            stopMovement();
            return;
        }

        Vec3 direction = pathSteering.update(bot.getDeltaMovement(), delta);
        double waypointSpeed = Math.min(movementSpeed, Math.max(0.12D, horizontalDistance * 0.45D));
        double verticalVelocity = bot.getDeltaMovement().y;
        if (bot.onGround() && targetPos.y > botPos.y + 0.35D) {
            verticalVelocity = Math.max(verticalVelocity, jumpVelocity);
        }

        bot.setDeltaMovement(direction.x * waypointSpeed, verticalVelocity, direction.z * waypointSpeed);
    }

    public void resetPathSteering() {
        pathSteering.reset();
    }

    @Override
    public void stopMovement() {
        bot.setDeltaMovement(0, bot.getDeltaMovement().y, 0);
        pathSteering.reset();
    }

    @Override
    public void ensureMovement() {
        if (bot.getDeltaMovement().horizontalDistance() < 0.05 && bot.onGround()) {
            double randomAngle = Math.random() * 2 * Math.PI;
            double smallMoveX = Math.cos(randomAngle) * movementSpeed * 0.75;
            double smallMoveZ = Math.sin(randomAngle) * movementSpeed * 0.75;
            bot.setDeltaMovement(smallMoveX, jumpVelocity * 0.65, smallMoveZ);
        }
    }

    private Vec3 findBestPath(Vec3 from, Vec3 to, double targetDistance) {
        Vec3 baseDirection = to.subtract(from).normalize();
        Vec3 targetPoint = to.subtract(baseDirection.scale(targetDistance));
        Vec3 desiredDirection = targetPoint.subtract(from).normalize();

        double[] angles = {0, Math.PI / 4, -Math.PI / 4, Math.PI / 2, -Math.PI / 2};

        for (double angle : angles) {
            Vec3 testDirection = rotateDirection(desiredDirection, angle);
            if (isPathSafeOptimized(from, testDirection, 2.0)) {
                return testDirection;
            }
        }
        return null;
    }

    private Vec3 rotateDirection(Vec3 direction, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Vec3(direction.x * cos - direction.z * sin, direction.y, direction.x * sin + direction.z * cos);
    }

    private boolean isPathSafeOptimized(Vec3 from, Vec3 direction, double distance) {
        int steps = Math.min((int) distance, 3);
        for (int i = 1; i <= steps; i++) {
            Vec3 checkPos = from.add(direction.scale(i));
            BlockPos blockPos = BlockPos.containing(checkPos);

            if (!blockValidator.isPositionPassableCached(blockPos)) {
                return false;
            }
        }
        return true;
    }

    public void setMovementSpeed(double speed) {
        this.movementSpeed = Math.max(0.1, Math.min(1.5, speed));
    }

    public double getMovementSpeed() {
        return movementSpeed;
    }

    public void setJumpVelocity(double velocity) {
        this.jumpVelocity = Math.max(0.2, Math.min(1.0, velocity));
    }

    public void setStrafeClockwise(boolean clockwise) {
        this.strafeClockwise = clockwise;
    }

    public void setStrafeAngle(double angle) {
        this.strafeAngle = angle;
    }

    public void setZigZagDirection(int direction) {
        this.zigZagDirection = direction;
        this.zigZagCounter = 0;
    }
}
