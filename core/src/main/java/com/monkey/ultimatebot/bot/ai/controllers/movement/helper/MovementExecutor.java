package com.monkey.ultimatebot.bot.ai.controllers.movement.helper;

import com.monkey.ultimatebot.bot.ai.controllers.movement.helper.interf.IBlockStateValidator;
import com.monkey.ultimatebot.bot.ai.controllers.movement.helper.interf.IMovementExecutor;
import com.monkey.ultimatebot.bot.ai.controllers.movement.helper.interf.IObstacleHandler;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.compat.EntityCoordsAccess;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

public class MovementExecutor implements IMovementExecutor {
    private static final int ZIG_ZAG_CHANGE_TICKS = 12;

    private final ITrainingBot bot;
    private final IBlockStateValidator blockValidator;
    private final IObstacleHandler obstacleHandler;

    private double movementSpeed = 0.25;
    private double jumpVelocity = 0.42;
    private double strafeAngle = 0.0;
    private boolean strafeClockwise = true;
    private int zigZagDirection = 1;
    private int zigZagCounter = 0;
    private final PathSteering pathSteering = new PathSteering();

    public MovementExecutor(ITrainingBot bot, IBlockStateValidator blockValidator, IObstacleHandler obstacleHandler) {
        this.bot = bot;
        this.blockValidator = blockValidator;
        this.obstacleHandler = obstacleHandler;
    }

    @Override
    public void executeDirectMovement(LivingEntity target, double targetDistance) {
        double targetX = EntityCoordsAccess.getX(target);
        double targetZ = EntityCoordsAccess.getZ(target);
        Vector botPos = bot.bukkitPosition();
        double botX = botPos.getX();
        double botZ = botPos.getZ();

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

        if (obstacleHandler.handleObstacles(dx, dz, botX, botPos.getY(), botZ, moveX, moveZ)) {
            return;
        }

        setVelocity(moveX, bot.bukkitVelocity().getY(), moveZ);
    }

    @Override
    public void executeStrafeCircle(LivingEntity target, double targetDistance) {
        Vector targetPos = target.getLocation().toVector();
        Vector botPos = bot.bukkitPosition();
        double angleSpeed = 0.08 + (Math.random() * 0.04);
        strafeAngle += strafeClockwise ? angleSpeed : -angleSpeed;

        double desiredX = targetPos.getX() + Math.cos(strafeAngle) * targetDistance;
        double desiredZ = targetPos.getZ() + Math.sin(strafeAngle) * targetDistance;
        Vector desiredPos = new Vector(desiredX, botPos.getY(), desiredZ);

        Vector direction = desiredPos.subtract(botPos).normalize();
        double moveX = direction.getX() * movementSpeed * 1.1;
        double moveZ = direction.getZ() * movementSpeed * 1.1;

        if (Math.random() < 0.01) {
            strafeClockwise = !strafeClockwise;
        }

        if (!obstacleHandler.handleObstacles(
                direction.getX(), direction.getZ(), botPos.getX(), botPos.getY(), botPos.getZ(), moveX, moveZ)) {
            setVelocity(moveX, bot.bukkitVelocity().getY(), moveZ);
        }
    }

    @Override
    public void executeStrafeFigure8(LivingEntity target, double targetDistance) {
        Vector targetPos = target.getLocation().toVector();
        Vector botPos = bot.bukkitPosition();

        strafeAngle += 0.12;
        double radiusX = targetDistance * 0.8;
        double radiusZ = targetDistance * 1.2;

        double desiredX = targetPos.getX() + Math.cos(strafeAngle) * radiusX;
        double desiredZ = targetPos.getZ() + Math.sin(strafeAngle * 2) * radiusZ;

        Vector desiredPos = new Vector(desiredX, botPos.getY(), desiredZ);
        Vector direction = desiredPos.subtract(botPos).normalize();

        double moveX = direction.getX() * movementSpeed * 1.05;
        double moveZ = direction.getZ() * movementSpeed * 1.05;

        if (!obstacleHandler.handleObstacles(
                direction.getX(), direction.getZ(), botPos.getX(), botPos.getY(), botPos.getZ(), moveX, moveZ)) {
            setVelocity(moveX, bot.bukkitVelocity().getY(), moveZ);
        }
    }

    @Override
    public void executeEvasiveZigZag(LivingEntity target, double targetDistance) {
        Vector targetPos = target.getLocation().toVector();
        Vector botPos = bot.bukkitPosition();

        zigZagCounter++;
        if (zigZagCounter >= ZIG_ZAG_CHANGE_TICKS) {
            zigZagDirection *= -1;
            zigZagCounter = 0;
            if (Math.random() < 0.3) {
                zigZagDirection *= Math.random() < 0.5 ? 1 : -1;
            }
        }

        Vector toTarget = targetPos.subtract(botPos);
        double distanceToTarget = toTarget.length();
        if (distanceToTarget == 0) return;

        Vector baseDirection = toTarget.normalize();

        if (distanceToTarget > targetDistance) {
            Vector perpendicular = new Vector(-baseDirection.getZ(), 0, baseDirection.getX());
            Vector zigzagDirection = baseDirection.clone().add(perpendicular.multiply(zigZagDirection * 0.7));
            zigzagDirection = zigzagDirection.normalize();

            double moveX = zigzagDirection.getX() * movementSpeed * 1.2;
            double moveZ = zigzagDirection.getZ() * movementSpeed * 1.2;

            if (!obstacleHandler.handleObstacles(
                    zigzagDirection.getX(),
                    zigzagDirection.getZ(),
                    botPos.getX(),
                    botPos.getY(),
                    botPos.getZ(),
                    moveX,
                    moveZ)) {
                setVelocity(moveX, bot.bukkitVelocity().getY(), moveZ);
            }
        } else {
            Vector perpendicular = new Vector(-baseDirection.getZ(), 0, baseDirection.getX());
            Vector strafeDir = perpendicular.multiply(zigZagDirection);

            double moveX = strafeDir.getX() * movementSpeed;
            double moveZ = strafeDir.getZ() * movementSpeed;

            if (!obstacleHandler.handleObstacles(
                    strafeDir.getX(), strafeDir.getZ(), botPos.getX(), botPos.getY(), botPos.getZ(), moveX, moveZ)) {
                setVelocity(moveX, bot.bukkitVelocity().getY(), moveZ);
            }
        }
    }

    @Override
    public void executeTerrainAdaptive(LivingEntity target, double targetDistance) {
        Vector targetPos = target.getLocation().toVector();
        Vector botPos = bot.bukkitPosition();

        Vector bestDirection = findBestPath(botPos, targetPos, targetDistance);
        if (bestDirection != null) {
            double moveX = bestDirection.getX() * movementSpeed;
            double moveZ = bestDirection.getZ() * movementSpeed;

            if (!obstacleHandler.handleObstacles(
                    bestDirection.getX(),
                    bestDirection.getZ(),
                    botPos.getX(),
                    botPos.getY(),
                    botPos.getZ(),
                    moveX,
                    moveZ)) {
                setVelocity(moveX, bot.bukkitVelocity().getY(), moveZ);
            }
        } else {
            executeDirectMovement(target, targetDistance);
        }
    }

    @Override
    public void executeRetreatSpiral(LivingEntity target, double targetDistance) {
        Vector targetPos = target.getLocation().toVector();
        Vector botPos = bot.bukkitPosition();

        strafeAngle += strafeClockwise ? 0.15 : -0.15;

        double currentDistance = botPos.distance(targetPos);
        double spiralRadius = Math.max(targetDistance, currentDistance + 1.0);
        spiralRadius += strafeAngle * 0.1;

        double desiredX = targetPos.getX() + Math.cos(strafeAngle) * spiralRadius;
        double desiredZ = targetPos.getZ() + Math.sin(strafeAngle) * spiralRadius;

        Vector desiredPos = new Vector(desiredX, botPos.getY(), desiredZ);
        Vector direction = desiredPos.subtract(botPos).normalize();

        double moveX = direction.getX() * movementSpeed * 1.15;
        double moveZ = direction.getZ() * movementSpeed * 1.15;

        if (!obstacleHandler.handleObstacles(
                direction.getX(), direction.getZ(), botPos.getX(), botPos.getY(), botPos.getZ(), moveX, moveZ)) {
            setVelocity(moveX, bot.bukkitVelocity().getY(), moveZ);
        }
    }

    @Override
    public void executeCrystalSpamMovement(LivingEntity target, double targetDistance) {
        Vector targetPos = target.getLocation().toVector();
        Vector botPos = bot.bukkitPosition();
        double yDiff = botPos.getY() - targetPos.getY();

        if (yDiff > -2.0) {
            Vector belowTarget = new Vector(targetPos.getX(), targetPos.getY() - 3, targetPos.getZ());
            Vector direction = belowTarget.subtract(botPos).normalize();
            double moveX = direction.getX() * movementSpeed * 1.3;
            double moveZ = direction.getZ() * movementSpeed * 1.3;
            setVelocity(moveX, bot.bukkitVelocity().getY(), moveZ);
        } else {
            executeStrafeCircle(target, targetDistance);
        }
    }

    @Override
    public void moveToPosition(Vector targetPos) {
        Vector botPos = bot.bukkitPosition();
        double dx = targetPos.getX() - botPos.getX();
        double dz = targetPos.getZ() - botPos.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);

        if (distance < 0.1) {
            stopMovement();
            return;
        }

        dx = dx / distance;
        dz = dz / distance;

        double moveX = dx * movementSpeed;
        double moveZ = dz * movementSpeed;

        if (obstacleHandler.handleObstacles(dx, dz, botPos.getX(), botPos.getY(), botPos.getZ(), moveX, moveZ)) {
            return;
        }

        setVelocity(moveX, bot.bukkitVelocity().getY(), moveZ);

        if (bot.isOnGround() && horizontalLength(bot.bukkitVelocity()) < 0.1) {
            ensureMovement();
        }
    }

    public void moveAlongPath(Vector targetPos) {
        Vector botPos = bot.bukkitPosition();
        Vector delta = targetPos.clone().subtract(botPos);
        double horizontalDistance = horizontalLength(delta);
        if (horizontalDistance < 0.05D) {
            stopMovement();
            return;
        }

        Vector direction = pathSteering.update(bot.bukkitVelocity(), delta);
        double waypointSpeed = Math.min(movementSpeed, Math.max(0.12D, horizontalDistance * 0.45D));
        double verticalVelocity = bot.bukkitVelocity().getY();
        if (bot.isOnGround() && targetPos.getY() > botPos.getY() + 0.35D) {
            verticalVelocity = Math.max(verticalVelocity, jumpVelocity);
        }

        setVelocity(direction.getX() * waypointSpeed, verticalVelocity, direction.getZ() * waypointSpeed);
    }

    public void resetPathSteering() {
        pathSteering.reset();
    }

    @Override
    public void stopMovement() {
        setVelocity(0, bot.bukkitVelocity().getY(), 0);
        pathSteering.reset();
    }

    @Override
    public void ensureMovement() {
        if (horizontalLength(bot.bukkitVelocity()) < 0.05 && bot.isOnGround()) {
            double randomAngle = Math.random() * 2 * Math.PI;
            double smallMoveX = Math.cos(randomAngle) * movementSpeed * 0.75;
            double smallMoveZ = Math.sin(randomAngle) * movementSpeed * 0.75;
            setVelocity(smallMoveX, jumpVelocity * 0.65, smallMoveZ);
        }
    }

    private @Nullable Vector findBestPath(Vector from, Vector to, double targetDistance) {
        Vector baseDirection = to.clone().subtract(from).normalize();
        Vector targetPoint = to.clone().subtract(baseDirection.clone().multiply(targetDistance));
        Vector desiredDirection = targetPoint.subtract(from).normalize();

        double[] angles = {0, Math.PI / 4, -Math.PI / 4, Math.PI / 2, -Math.PI / 2};

        for (double angle : angles) {
            Vector testDirection = rotateDirection(desiredDirection, angle);
            if (isPathSafeOptimized(from, testDirection, 2.0)) {
                return testDirection;
            }
        }
        return null;
    }

    private Vector rotateDirection(Vector direction, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Vector(
                direction.getX() * cos - direction.getZ() * sin,
                direction.getY(),
                direction.getX() * sin + direction.getZ() * cos);
    }

    private boolean isPathSafeOptimized(Vector from, Vector direction, double distance) {
        int steps = Math.min((int) distance, 3);
        for (int i = 1; i <= steps; i++) {
            Vector checkPos = from.clone().add(direction.clone().multiply(i));
            BlockVector blockPos = blockAt(checkPos);

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

    private void setVelocity(double x, double y, double z) {
        bot.setBukkitVelocity(new Vector(x, y, z));
    }

    private static double horizontalLength(Vector vector) {
        return Math.hypot(vector.getX(), vector.getZ());
    }

    private static BlockVector blockAt(Vector position) {
        return new BlockVector(
                (int) Math.floor(position.getX()), (int) Math.floor(position.getY()), (int) Math.floor(position.getZ()));
    }
}
