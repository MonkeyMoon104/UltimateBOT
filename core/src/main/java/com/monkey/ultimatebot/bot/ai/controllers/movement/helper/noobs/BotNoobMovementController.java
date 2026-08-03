package com.monkey.ultimatebot.bot.ai.controllers.movement.helper.noobs;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class BotNoobMovementController {

    private final Player bot;
    private final Level level;

    private double @Nullable [] diversionDirection;
    private int diversionTicks = 0;

    private double movementSpeed = 0.25;
    private double jumpVelocity = 0.42;

    public BotNoobMovementController(Player bot, Level level) {
        this.bot = bot;
        this.level = level;
    }

    public void moveTowards(Player target, double targetDistance) {
        double targetX = target.getX();
        double targetZ = target.getZ();

        double botX = bot.getX();
        double botY = bot.getY();
        double botZ = bot.getZ();

        double dx = targetX - botX;
        double dz = targetZ - botZ;

        double currentDistance = Math.sqrt(dx * dx + dz * dz);
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

        if (handleObstacles(dx, dz, botX, botY, botZ)) {
            return;
        }

        bot.setDeltaMovement(moveX, bot.getDeltaMovement().y, moveZ);
    }

    public void moveAwayFrom(Player target, double targetDistance) {
        double targetX = target.getX();
        double targetZ = target.getZ();

        double botX = bot.getX();
        double botZ = bot.getZ();

        double dx = botX - targetX;
        double dz = botZ - targetZ;

        double currentDistance = Math.sqrt(dx * dx + dz * dz);
        if (currentDistance == 0) {
            dx = Math.random() - 0.5;
            dz = Math.random() - 0.5;
            currentDistance = Math.sqrt(dx * dx + dz * dz);
        }

        dx = dx / currentDistance;
        dz = dz / currentDistance;

        double moveX = dx * movementSpeed;
        double moveZ = dz * movementSpeed;

        if (handleObstacles(dx, dz, botX, bot.getY(), botZ)) {
            return;
        }

        bot.setDeltaMovement(moveX, bot.getDeltaMovement().y, moveZ);
    }

    private boolean handleObstacles(double dx, double dz, double botX, double botY, double botZ) {
        BlockPos front = BlockPos.containing(botX + dx, botY, botZ + dz);
        BlockPos above = front.above();
        BlockPos above2 = above.above();

        boolean frontBlocked =
                !level.getBlockState(front).getCollisionShape(level, front).isEmpty();
        boolean aboveClear =
                level.getBlockState(above).getCollisionShape(level, above).isEmpty();
        boolean above2Clear =
                level.getBlockState(above2).getCollisionShape(level, above2).isEmpty();

        boolean canStepUp = frontBlocked && aboveClear;
        boolean tooHigh = frontBlocked && !aboveClear && !above2Clear;

        if (tooHigh) {
            handleHighObstacle(dx, dz);
            return true;
        }

        if (canStepUp && bot.onGround()) {
            bot.setDeltaMovement(bot.getDeltaMovement().x, jumpVelocity, bot.getDeltaMovement().z);
        }

        return false;
    }

    private void handleHighObstacle(double dx, double dz) {
        if (diversionTicks <= 0 || diversionDirection == null) {
            diversionDirection = findAlternativeDirection(dx, dz, 8);
            diversionTicks = 15;
        }

        if (diversionDirection != null) {
            diversionTicks--;
            double altDx = diversionDirection[0];
            double altDz = diversionDirection[1];
            bot.setDeltaMovement(altDx * movementSpeed, bot.getDeltaMovement().y, altDz * movementSpeed);
        } else {
            bot.setDeltaMovement(0, bot.getDeltaMovement().y, 0);
        }

        if (diversionTicks <= 0) {
            diversionDirection = null;
        }
    }

    private double @Nullable [] findAlternativeDirection(double dx, double dz, int maxTries) {
        double angle = Math.atan2(dz, dx);

        for (int i = 1; i <= maxTries; i++) {
            double offset = Math.toRadians(12 * i);

            for (int sign : new int[] {1, -1}) {
                double newAngle = angle + offset * sign;

                double newDx = Math.cos(newAngle);
                double newDz = Math.sin(newAngle);

                if (isPathClear(newDx, newDz)) {
                    return new double[] {newDx, newDz};
                }
            }
        }

        return null;
    }

    private boolean isPathClear(double dx, double dz) {
        BlockPos checkPos = BlockPos.containing(bot.getX() + dx, bot.getY(), bot.getZ() + dz);
        BlockPos checkAbove = checkPos.above();
        BlockPos checkAbove2 = checkAbove.above();

        boolean frontClear =
                level.getBlockState(checkPos).getCollisionShape(level, checkPos).isEmpty();
        boolean aboveClear = level.getBlockState(checkAbove)
                .getCollisionShape(level, checkAbove)
                .isEmpty();
        boolean above2Clear = level.getBlockState(checkAbove2)
                .getCollisionShape(level, checkAbove2)
                .isEmpty();

        return frontClear && aboveClear && above2Clear;
    }

    public void setMovementSpeed(double speed) {
        this.movementSpeed = Math.max(0.1, Math.min(1.0, speed));
    }

    public void setJumpVelocity(double velocity) {
        this.jumpVelocity = Math.max(0.2, Math.min(1.0, velocity));
    }

    public void stopMovement() {
        bot.setDeltaMovement(0, bot.getDeltaMovement().y, 0);
        diversionDirection = null;
        diversionTicks = 0;
    }

    public boolean isDiverting() {
        return diversionTicks > 0 && diversionDirection != null;
    }
}
