package com.monkey.ultimatebot.bot.ai.controllers.movement.helper;

import com.monkey.ultimatebot.bot.ai.controllers.movement.helper.interf.IBlockStateValidator;
import com.monkey.ultimatebot.bot.ai.controllers.movement.helper.interf.IObstacleHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class ObstacleHandler implements IObstacleHandler {
    private final Player bot;
    private final Level level;
    private final IBlockStateValidator blockValidator;
    private final double movementSpeed;
    private final double jumpVelocity;

    private double[] diversionDirection = null;
    private int diversionTicks = 0;
    private boolean isUnderFire = false;

    public ObstacleHandler(
            Player bot, Level level, IBlockStateValidator blockValidator, double movementSpeed, double jumpVelocity) {
        this.bot = bot;
        this.level = level;
        this.blockValidator = blockValidator;
        this.movementSpeed = movementSpeed;
        this.jumpVelocity = jumpVelocity;
    }

    @Override
    public boolean handleObstacles(
            double dx, double dz, double botX, double botY, double botZ, double moveX, double moveZ) {
        BlockPos immediate = BlockPos.containing(botX + dx * 0.7, botY, botZ + dz * 0.7);
        BlockPos immediateHead = immediate.above();
        if (isCobweb(immediate) || isCobweb(immediateHead) || isCobweb(immediate.below())) {
            handleHighObstacle(dx, dz);
            return true;
        }
        if (!blockValidator.isPositionPassableCached(immediate)) {
            if (bot.onGround() && blockValidator.isPositionPassableCached(immediateHead)) {
                bot.setDeltaMovement(moveX * 1.15, jumpVelocity, moveZ * 1.15);
            } else {
                handleHighObstacle(dx, dz);
            }
            return true;
        }

        BlockPos front = BlockPos.containing(botX + dx * 2, botY, botZ + dz * 2);
        BlockPos above = front.above();
        BlockPos below = front.below();

        boolean frontBlocked = !blockValidator.isPositionPassableCached(front);
        boolean aboveClear = blockValidator.isPositionPassableCached(above);
        boolean belowSolid = !level.getBlockState(below).isAir();

        boolean canStepUp = frontBlocked && aboveClear && belowSolid;
        boolean tooHigh = frontBlocked && !aboveClear;

        if (tooHigh) {
            handleHighObstacle(dx, dz);
            return true;
        }

        if (canStepUp && bot.onGround()) {
            bot.setDeltaMovement(moveX * 1.2, jumpVelocity, moveZ * 1.2);
            return true;
        }

        return false;
    }

    private void handleHighObstacle(double dx, double dz) {
        if (diversionTicks <= 0 || diversionDirection == null) {
            diversionDirection = findAlternativeDirection(dx, dz, 4);
            diversionTicks = 20;
        }

        if (diversionDirection != null) {
            diversionTicks--;
            double altDx = diversionDirection[0];
            double altDz = diversionDirection[1];
            double diversionSpeed = isUnderFire ? movementSpeed * 1.3 : movementSpeed;
            bot.setDeltaMovement(altDx * diversionSpeed, bot.getDeltaMovement().y, altDz * diversionSpeed);
        } else {
            bot.setDeltaMovement(0, bot.getDeltaMovement().y, 0);
        }

        if (diversionTicks <= 0) {
            diversionDirection = null;
        }
    }

    private double[] findAlternativeDirection(double dx, double dz, int maxTries) {
        double angle = Math.atan2(dz, dx);

        for (int i = 1; i <= maxTries; i++) {
            double offset = Math.toRadians(20 * i);
            for (int sign : new int[] {1, -1}) {
                double newAngle = angle + offset * sign;
                double newDx = Math.cos(newAngle);
                double newDz = Math.sin(newAngle);

                if (isPathClearOptimized(newDx, newDz)) {
                    return new double[] {newDx, newDz};
                }
            }
        }
        return null;
    }

    private boolean isPathClearOptimized(double dx, double dz) {
        BlockPos checkPos = BlockPos.containing(bot.getX() + dx * 2, bot.getY(), bot.getZ() + dz * 2);
        return !isCobweb(checkPos)
                && !isCobweb(checkPos.above())
                && !isCobweb(checkPos.below())
                && blockValidator.isPositionPassableCached(checkPos);
    }

    private boolean isCobweb(BlockPos position) {
        return level.getBlockState(position).is(Blocks.COBWEB);
    }

    @Override
    public boolean hasComplexTerrain(Vec3 position) {
        BlockPos centerPos = BlockPos.containing(position);
        int solidBlocks = 0;

        BlockPos[] checkPositions = {centerPos.north(), centerPos.south(), centerPos.east(), centerPos.west()};

        for (BlockPos checkPos : checkPositions) {
            if (!blockValidator.isPositionPassableCached(checkPos)) {
                solidBlocks++;
            }
        }

        return solidBlocks >= 2;
    }

    @Override
    public boolean hasObstacles(Vec3 from, Vec3 to) {
        Vec3 direction = to.subtract(from).normalize();
        double distance = from.distanceTo(to);
        int steps = Math.min((int) (distance / 3.0), 5);

        for (int i = 1; i <= steps; i++) {
            Vec3 checkPos = from.add(direction.scale(i * 3.0));
            BlockPos blockPos = BlockPos.containing(checkPos);

            if (!blockValidator.isPositionPassableCached(blockPos)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isDiverting() {
        return diversionTicks > 0 && diversionDirection != null;
    }

    @Override
    public void setUnderFire(boolean underFire) {
        this.isUnderFire = underFire;
    }

    @Override
    public void clearDiversion() {
        diversionDirection = null;
        diversionTicks = 0;
    }
}
