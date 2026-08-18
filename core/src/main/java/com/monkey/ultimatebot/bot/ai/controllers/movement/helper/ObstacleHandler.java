package com.monkey.ultimatebot.bot.ai.controllers.movement.helper;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.controllers.movement.helper.interf.IBlockStateValidator;
import com.monkey.ultimatebot.bot.ai.controllers.movement.helper.interf.IObstacleHandler;
import com.monkey.ultimatebot.utils.material.MaterialCatalog;
import org.bukkit.World;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

public class ObstacleHandler implements IObstacleHandler {
    private final ITrainingBot bot;
    private final World world;
    private final IBlockStateValidator blockValidator;
    private final double movementSpeed;
    private final double jumpVelocity;

    private double @Nullable [] diversionDirection;
    private int diversionTicks = 0;
    private boolean isUnderFire = false;

    public ObstacleHandler(
            ITrainingBot bot, IBlockStateValidator blockValidator, double movementSpeed, double jumpVelocity) {
        this.bot = bot;
        this.world = bot.getWorld();
        this.blockValidator = blockValidator;
        this.movementSpeed = movementSpeed;
        this.jumpVelocity = jumpVelocity;
    }

    @Override
    public boolean handleObstacles(
            double dx, double dz, double botX, double botY, double botZ, double moveX, double moveZ) {
        BlockVector immediate = blockAt(botX + dx * 0.7, botY, botZ + dz * 0.7);
        BlockVector immediateHead = above(immediate);
        if (isCobweb(immediate) || isCobweb(immediateHead) || isCobweb(below(immediate))) {
            handleHighObstacle(dx, dz);
            return true;
        }
        if (!blockValidator.isPositionPassableCached(immediate)) {
            if (bot.isOnGround() && blockValidator.isPositionPassableCached(immediateHead)) {
                setVelocity(moveX * 1.15, jumpVelocity, moveZ * 1.15);
            } else {
                handleHighObstacle(dx, dz);
            }
            return true;
        }

        BlockVector front = blockAt(botX + dx * 2, botY, botZ + dz * 2);
        BlockVector above = above(front);
        BlockVector below = below(front);

        boolean frontBlocked = !blockValidator.isPositionPassableCached(front);
        boolean aboveClear = blockValidator.isPositionPassableCached(above);
        boolean belowSolid = !world.getBlockAt(below.getBlockX(), below.getBlockY(), below.getBlockZ())
                .isEmpty();

        boolean canStepUp = frontBlocked && aboveClear && belowSolid;
        boolean tooHigh = frontBlocked && !aboveClear;

        if (tooHigh) {
            handleHighObstacle(dx, dz);
            return true;
        }

        if (canStepUp && bot.isOnGround()) {
            setVelocity(moveX * 1.2, jumpVelocity, moveZ * 1.2);
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
            setVelocity(altDx * diversionSpeed, bot.bukkitVelocity().getY(), altDz * diversionSpeed);
        } else {
            setVelocity(0, bot.bukkitVelocity().getY(), 0);
        }

        if (diversionTicks <= 0) {
            diversionDirection = null;
        }
    }

    private double @Nullable [] findAlternativeDirection(double dx, double dz, int maxTries) {
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
        Vector position = bot.bukkitPosition();
        BlockVector checkPos = blockAt(position.getX() + dx * 2, position.getY(), position.getZ() + dz * 2);
        return !isCobweb(checkPos)
                && !isCobweb(above(checkPos))
                && !isCobweb(below(checkPos))
                && blockValidator.isPositionPassableCached(checkPos);
    }

    private boolean isCobweb(BlockVector position) {
        return MaterialCatalog.is(
                world.getBlockAt(position.getBlockX(), position.getBlockY(), position.getBlockZ())
                        .getType(),
                "COBWEB");
    }

    @Override
    public boolean hasComplexTerrain(Vector position) {
        BlockVector centerPos = blockAt(position);
        int solidBlocks = 0;

        BlockVector[] checkPositions = {north(centerPos), south(centerPos), east(centerPos), west(centerPos)};

        for (BlockVector checkPos : checkPositions) {
            if (!blockValidator.isPositionPassableCached(checkPos)) {
                solidBlocks++;
            }
        }

        return solidBlocks >= 2;
    }

    @Override
    public boolean hasObstacles(Vector from, Vector to) {
        Vector direction = to.clone().subtract(from).normalize();
        double distance = from.distance(to);
        int steps = Math.min((int) (distance / 3.0), 5);

        for (int i = 1; i <= steps; i++) {
            Vector checkPos = from.clone().add(direction.clone().multiply(i * 3.0));
            BlockVector blockPos = blockAt(checkPos);

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

    private void setVelocity(double x, double y, double z) {
        bot.setBukkitVelocity(new Vector(x, y, z));
    }

    private static BlockVector blockAt(Vector position) {
        return blockAt(position.getX(), position.getY(), position.getZ());
    }

    private static BlockVector blockAt(double x, double y, double z) {
        return new BlockVector((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
    }

    private static BlockVector above(BlockVector position) {
        return new BlockVector(position.getBlockX(), position.getBlockY() + 1, position.getBlockZ());
    }

    private static BlockVector below(BlockVector position) {
        return new BlockVector(position.getBlockX(), position.getBlockY() - 1, position.getBlockZ());
    }

    private static BlockVector north(BlockVector position) {
        return new BlockVector(position.getBlockX(), position.getBlockY(), position.getBlockZ() - 1);
    }

    private static BlockVector south(BlockVector position) {
        return new BlockVector(position.getBlockX(), position.getBlockY(), position.getBlockZ() + 1);
    }

    private static BlockVector east(BlockVector position) {
        return new BlockVector(position.getBlockX() + 1, position.getBlockY(), position.getBlockZ());
    }

    private static BlockVector west(BlockVector position) {
        return new BlockVector(position.getBlockX() - 1, position.getBlockY(), position.getBlockZ());
    }
}
