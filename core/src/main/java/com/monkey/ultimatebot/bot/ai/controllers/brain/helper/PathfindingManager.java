package com.monkey.ultimatebot.bot.ai.controllers.brain.helper;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.compat.MaterialAirAccess;
import com.monkey.ultimatebot.bot.ai.controllers.brain.helper.inter.IPathfindingManager;
import com.monkey.ultimatebot.bot.ai.controllers.enderpearl.BotEnderpearlController;
import com.monkey.ultimatebot.bot.ai.controllers.movement.BotMovementController;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.BlockVector;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

@SuppressWarnings({"StringConcatToTextBlock", "FloatingPointLiteralPrecision", "NullAway"})
public class PathfindingManager implements IPathfindingManager {

    private final ITrainingBot bot;
    private final BotMovementController movementController;
    private final BotEnderpearlController enderpearlController;

    private Vector lastBotPosition;
    private int stuckCounter = 0;
    private static final double MIN_MOVEMENT_THRESHOLD = 0.1;

    private long lastPathfindingAttempt = 0;
    private static final long PATHFINDING_ATTEMPT_COOLDOWN = 2_500;
    private static final int BLOCKED_PATH_CONFIRMATION_TICKS = 2;
    private long lastStuckPearlAttempt = 0;
    private static final long STUCK_PEARL_COOLDOWN = 1200;
    private boolean usingPathfinding = false;
    private int blockedPathCounter;

    public PathfindingManager(
            ITrainingBot bot,
            BotMovementController movementController,
            BotEnderpearlController enderpearlController) {
        this.bot = bot;
        this.movementController = movementController;
        this.enderpearlController = enderpearlController;
        this.lastBotPosition = bot.bukkitPosition();
    }

    @Override
    public void checkForStuck(LivingEntity target) {
        Vector currentPos = bot.bukkitPosition();
        double movementDistance = currentPos.distance(lastBotPosition);

        if (movementDistance < MIN_MOVEMENT_THRESHOLD) {
            stuckCounter++;
        } else {
            stuckCounter = 0;
        }

        boolean directPathBlocked = movementController.isPathObstructed(target.getLocation().toVector());
        blockedPathCounter = directPathBlocked ? blockedPathCounter + 1 : 0;

        if (blockedPathCounter >= BLOCKED_PATH_CONFIRMATION_TICKS) {
            attemptPathfinding(target, false);
            blockedPathCounter = 0;
            return;
        }

        if (stuckCounter > 20) {
            attemptPathfinding(target, true);
            stuckCounter = 0;
        }
    }

    @Override
    public void attemptPathfindingOrPearl(LivingEntity target) {
        attemptPathfinding(target, true);
    }

    private void attemptPathfinding(LivingEntity target, boolean allowRecovery) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastPathfindingAttempt < PATHFINDING_ATTEMPT_COOLDOWN) {
            if (allowRecovery && shouldAttemptStuckPearl(target, currentTime)) {
                tryUnstuckPearl(target, currentTime);
            }
            return;
        }

        lastPathfindingAttempt = currentTime;

        if (movementController.calculatePathTo(target.getLocation().toVector())) {
            usingPathfinding = true;
            movementController.followPath();
            return;
        }

        if (allowRecovery
                && enderpearlController.canUseEnderpearl()
                && bot.distanceTo(target) > 4.0
                && hasObstacleBetween(bot.bukkitPosition(), target.getLocation().toVector())) {
            if (tryUnstuckPearl(target, currentTime)) {
                return;
            }
        }

        if (allowRecovery) {
            forceUnstuck(target);
        }
    }

    @Override
    public boolean hasObstacleBetween(Vector start, Vector end) {
        Vector delta = end.clone().subtract(start);
        double distance = delta.length();
        if (distance < 1.0E-6D) {
            return false;
        }
        Vector direction = delta.normalize();
        int steps = (int) (distance * 2);

        for (int i = 1; i < steps; i++) {
            Vector checkPos = start.clone().add(direction.clone().multiply(i * 0.5D));
            Block block = blockAt(checkPos);
            if (isSolid(block) || isSolid(block.getRelative(0, 1, 0))) {
                return true;
            }
        }

        return false;
    }

    @Override
    @SuppressWarnings("FloatingPointLiteralPrecision")
    public @Nullable Vector calculatePearlTargetAroundPlayer(LivingEntity target) {
        Vector targetPos = target.getLocation().toVector();
        Vector botPos = bot.bukkitPosition();
        double baseAngle = Math.atan2(botPos.getZ() - targetPos.getZ(), botPos.getX() - targetPos.getX());

        double[] radii = {14.0D / 5.0D, 17.0D / 5.0D, 41.0D / 10.0D, 24.0D / 5.0D, 27.0D / 5.0D};
        double[] angleOffsets = {0D, 25D, -25D, 50D, -50D, 80D, -80D, 110D, -110D, 180D};
        int[] yOffsets = {0, -1, 1};

        Vector best = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (double radius : radii) {
            for (double offsetDeg : angleOffsets) {
                double angle = baseAngle + Math.toRadians(offsetDeg);
                double x = targetPos.getX() + Math.cos(angle) * radius;
                double z = targetPos.getZ() + Math.sin(angle) * radius;

                for (int yOffset : yOffsets) {
                    BlockVector blockPos = new BlockVector(x, targetPos.getY() + yOffset, z);
                    if (!isSafeLandingSpot(blockPos)) {
                        continue;
                    }

                    Vector center = centerOf(blockPos);
                    if (!hasThrowPath(center)) {
                        continue;
                    }

                    double score = evaluatePearlTargetScore(center, targetPos, botPos);
                    if (score > bestScore) {
                        bestScore = score;
                        best = center;
                    }
                }
            }
        }

        return best != null ? best : calculatePearlSideStepTarget(target);
    }

    @Override
    public boolean isSafeLandingSpot(BlockVector pos) {
        Block block = blockAt(pos);
        Block below = block.getRelative(0, -1, 0);
        Block above = block.getRelative(0, 1, 0);
        return isSolid(below) && isPassable(block) && isPassable(above);
    }

    @Override
    public void forceUnstuck(LivingEntity target) {
        double distance = bot.distanceTo(target);
        Vector botPos = bot.bukkitPosition();
        Vector toTarget = target.getLocation().toVector().subtract(botPos);
        toTarget.setY(0.0D);
        if (toTarget.lengthSquared() < 1.0E-5D) {
            toTarget = bot.getLookDirection();
            toTarget.setY(0.0D);
        }
        Vector horizontalDirection = normalizeOrFallback(toTarget, new Vector(1.0D, 0.0D, 0.0D));

        if (distance <= 1.0) {
            movementController.moveToPosition(botPos.subtract(horizontalDirection.multiply(3.0D)));
        } else if (distance >= 10.0) {
            movementController.moveToPosition(botPos.add(horizontalDirection.multiply(3.0D)));
        } else {
            Vector strafeDirection = getStrafeDirection(target);
            if (isFacingSolidWall()) {
                strafeDirection.multiply(-1.0D);
            }
            movementController.moveToPosition(botPos.add(strafeDirection.multiply(2.0D)));
        }
    }

    private Vector getStrafeDirection(LivingEntity target) {
        Vector toTarget = target.getLocation().toVector().subtract(bot.bukkitPosition());
        toTarget.setY(0.0D);
        Vector normalized = normalizeOrFallback(toTarget, new Vector(1.0D, 0.0D, 0.0D));
        return new Vector(-normalized.getZ(), 0, normalized.getX());
    }

    @Override
    public boolean isUsingPathfinding() {
        return usingPathfinding;
    }

    @Override
    public void setUsingPathfinding(boolean using) {
        this.usingPathfinding = using;
    }

    public void updateLastBotPosition() {
        this.lastBotPosition = bot.bukkitPosition();
    }

    private boolean shouldAttemptStuckPearl(LivingEntity target, long currentTime) {
        if (!enderpearlController.canUseEnderpearl()) {
            return false;
        }
        if (currentTime - lastStuckPearlAttempt < STUCK_PEARL_COOLDOWN) {
            return false;
        }

        double distance = bot.distanceTo(target);
        if (distance < 2.2D || distance > 15.0D) {
            return false;
        }

        return hasObstacleBetween(bot.bukkitPosition(), target.getLocation().toVector())
                || isFacingSolidWall()
                || stuckCounter > 18;
    }

    private boolean tryUnstuckPearl(LivingEntity target, long currentTime) {
        Vector pearlTarget = calculatePearlTargetAroundPlayer(target);
        if (pearlTarget == null) {
            return false;
        }

        if (enderpearlController.tryUseEnderpearlToPosition(pearlTarget)) {
            lastStuckPearlAttempt = currentTime;
            usingPathfinding = false;
            movementController.clearPath();
            return true;
        }
        return false;
    }

    private @Nullable Vector calculatePearlSideStepTarget(LivingEntity target) {
        Vector botPos = bot.bukkitPosition();
        Vector toTarget = target.getLocation().toVector().subtract(botPos);
        toTarget.setY(0.0D);
        if (toTarget.lengthSquared() < 1.0E-5D) {
            toTarget = bot.getLookDirection();
            toTarget.setY(0.0D);
        }
        Vector lateral = new Vector(-toTarget.getZ(), 0.0D, toTarget.getX());
        if (lateral.lengthSquared() < 1.0E-5D) {
            return null;
        }
        lateral.normalize();

        double[] scales = {3.2D, 4.0D, 4.8D};
        for (double scale : scales) {
            for (int sign : new int[] {1, -1}) {
                Vector candidate = botPos.clone().add(lateral.clone().multiply(scale * sign)).add(new Vector(0.0D, 0.6D, 0.0D));
                BlockVector blockPos = toBlockVector(candidate);
                if (!isSafeLandingSpot(blockPos)) {
                    continue;
                }
                Vector center = centerOf(blockPos);
                if (hasThrowPath(center)) {
                    return center;
                }
            }
        }
        return null;
    }

    private double evaluatePearlTargetScore(Vector candidate, Vector targetPos, Vector botPos) {
        double distToTarget = candidate.distance(targetPos);
        double distToBot = candidate.distance(botPos);
        double score = 0.0D;
        score -= Math.abs(distToTarget - 3.6D) * 2.0D;
        score -= Math.abs(distToBot - 5.0D) * 0.9D;

        if (!hasObstacleBetween(botPos, candidate)) {
            score += 1.4D;
        }
        if (!hasObstacleBetween(candidate, targetPos)) {
            score += 0.9D;
        }

        return score;
    }

    private boolean isFacingSolidWall() {
        Vector eyes = bot.asBukkitPlayer().getEyeLocation().toVector();
        Vector look = normalizeOrFallback(bot.getLookDirection(), null);
        if (look == null) {
            return false;
        }

        for (double step = 0.8D; step <= 1.8D; step += 0.5D) {
            Vector check = eyes.clone().add(look.clone().multiply(step));
            Block block = blockAt(check);
            if (isSolid(block) || isSolid(block.getRelative(0, 1, 0))) {
                return true;
            }
        }
        return false;
    }

    private boolean hasThrowPath(Vector destination) {
        Vector eyes = bot.asBukkitPlayer().getEyeLocation().toVector();
        Vector delta = destination.clone().subtract(eyes);
        double distance = delta.length();
        if (distance < 1.0E-6D) {
            return true;
        }
        Vector direction = delta.normalize();
        RayTraceResult result = bot.getWorld().rayTraceBlocks(
                bot.asBukkitPlayer().getEyeLocation(),
                direction,
                distance,
                FluidCollisionMode.NEVER,
                true);
        if (result == null || result.getHitBlock() == null) {
            return true;
        }

        BlockVector destinationBlock = toBlockVector(destination);
        Block hitBlock = result.getHitBlock();
        return blockEquals(hitBlock, destinationBlock)
                || blockEquals(hitBlock.getRelative(0, -1, 0), destinationBlock)
                || blockEquals(hitBlock.getRelative(0, 1, 0), destinationBlock);
    }

    private Block blockAt(Vector value) {
        World world = bot.getWorld();
        return world.getBlockAt(value.getBlockX(), value.getBlockY(), value.getBlockZ());
    }

    private Block blockAt(BlockVector value) {
        World world = bot.getWorld();
        return world.getBlockAt(value.getBlockX(), value.getBlockY(), value.getBlockZ());
    }

    private static BlockVector toBlockVector(Vector value) {
        return new BlockVector(value.getBlockX(), value.getBlockY(), value.getBlockZ());
    }

    private static Vector centerOf(BlockVector value) {
        return new Vector(value.getBlockX() + 0.5D, value.getBlockY() + 0.5D, value.getBlockZ() + 0.5D);
    }

    private static boolean blockEquals(Block block, BlockVector vector) {
        return block.getX() == vector.getBlockX()
                && block.getY() == vector.getBlockY()
                && block.getZ() == vector.getBlockZ();
    }

    private static boolean isPassable(Block block) {
        Material type = block.getType();
        return MaterialAirAccess.isAir(type) || block.isPassable();
    }

    private static boolean isSolid(Block block) {
        Material type = block.getType();
        return type.isBlock() && type.isSolid() && !block.isPassable();
    }

    private static @Nullable Vector normalizeOrFallback(Vector vector, @Nullable Vector fallback) {
        if (vector != null && vector.lengthSquared() > 1.0E-5D) {
            return vector.normalize();
        }
        return fallback;
    }
}
