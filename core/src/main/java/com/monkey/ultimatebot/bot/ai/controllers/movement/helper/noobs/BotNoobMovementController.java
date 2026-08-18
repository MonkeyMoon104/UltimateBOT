package com.monkey.ultimatebot.bot.ai.controllers.movement.helper.noobs;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.access.block.BlockPassableAccess;
import com.monkey.ultimatebot.access.entity.EntityCoordsAccess;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

public class BotNoobMovementController {

    private final ITrainingBot bot;
    private final World world;

    private double @Nullable [] diversionDirection;
    private int diversionTicks = 0;

    private double movementSpeed = 0.25;
    private double jumpVelocity = 0.42;

    public BotNoobMovementController(ITrainingBot bot) {
        this.bot = bot;
        this.world = bot.getWorld();
    }

    public void moveTowards(LivingEntity target, double targetDistance) {
        double targetX = EntityCoordsAccess.getX(target);
        double targetZ = EntityCoordsAccess.getZ(target);

        Vector botPos = bot.bukkitPosition();
        double botX = botPos.getX();
        double botY = botPos.getY();
        double botZ = botPos.getZ();

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

        setVelocity(moveX, bot.bukkitVelocity().getY(), moveZ);
    }

    public void moveAwayFrom(LivingEntity target, double targetDistance) {
        double targetX = EntityCoordsAccess.getX(target);
        double targetZ = EntityCoordsAccess.getZ(target);

        Vector botPos = bot.bukkitPosition();
        double botX = botPos.getX();
        double botZ = botPos.getZ();

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

        if (handleObstacles(dx, dz, botX, botPos.getY(), botZ)) {
            return;
        }

        setVelocity(moveX, bot.bukkitVelocity().getY(), moveZ);
    }

    private boolean handleObstacles(double dx, double dz, double botX, double botY, double botZ) {
        BlockVector front = blockAt(botX + dx, botY, botZ + dz);
        BlockVector above = above(front);
        BlockVector above2 = above(above);

        boolean frontBlocked = !BlockPassableAccess.isPassable(
                world.getBlockAt(front.getBlockX(), front.getBlockY(), front.getBlockZ()));
        boolean aboveClear = BlockPassableAccess.isPassable(
                world.getBlockAt(above.getBlockX(), above.getBlockY(), above.getBlockZ()));
        boolean above2Clear = BlockPassableAccess.isPassable(
                world.getBlockAt(above2.getBlockX(), above2.getBlockY(), above2.getBlockZ()));

        boolean canStepUp = frontBlocked && aboveClear;
        boolean tooHigh = frontBlocked && !aboveClear && !above2Clear;

        if (tooHigh) {
            handleHighObstacle(dx, dz);
            return true;
        }

        if (canStepUp && bot.isOnGround()) {
            setVelocity(dx * movementSpeed * 1.15D, jumpVelocity, dz * movementSpeed * 1.15D);
            return true;
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
            setVelocity(altDx * movementSpeed, bot.bukkitVelocity().getY(), altDz * movementSpeed);
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
        Vector botPos = bot.bukkitPosition();
        BlockVector checkPos = blockAt(botPos.getX() + dx, botPos.getY(), botPos.getZ() + dz);
        BlockVector checkAbove = above(checkPos);
        BlockVector checkAbove2 = above(checkAbove);

        boolean frontClear = BlockPassableAccess.isPassable(
                world.getBlockAt(checkPos.getBlockX(), checkPos.getBlockY(), checkPos.getBlockZ()));
        boolean aboveClear = BlockPassableAccess.isPassable(
                world.getBlockAt(checkAbove.getBlockX(), checkAbove.getBlockY(), checkAbove.getBlockZ()));
        boolean above2Clear = BlockPassableAccess.isPassable(
                world.getBlockAt(checkAbove2.getBlockX(), checkAbove2.getBlockY(), checkAbove2.getBlockZ()));

        return frontClear && aboveClear && above2Clear;
    }

    public void setMovementSpeed(double speed) {
        this.movementSpeed = Math.max(0.1, Math.min(1.0, speed));
    }

    public void setJumpVelocity(double velocity) {
        this.jumpVelocity = Math.max(0.2, Math.min(1.0, velocity));
    }

    public void stopMovement() {
        setVelocity(0, bot.bukkitVelocity().getY(), 0);
        diversionDirection = null;
        diversionTicks = 0;
    }

    public boolean isDiverting() {
        return diversionTicks > 0 && diversionDirection != null;
    }

    private void setVelocity(double x, double y, double z) {
        bot.setBukkitVelocity(new Vector(x, y, z));
    }

    private static BlockVector blockAt(double x, double y, double z) {
        return new BlockVector((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
    }

    private static BlockVector above(BlockVector position) {
        return new BlockVector(position.getBlockX(), position.getBlockY() + 1, position.getBlockZ());
    }
}
