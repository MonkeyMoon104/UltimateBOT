package com.monkey.mcbot.bot.ai.controllers.brain.helper;

import com.monkey.mcbot.bot.ai.controllers.brain.helper.inter.IPathfindingManager;
import com.monkey.mcbot.bot.ai.controllers.enderpearl.BotEnderpearlController;
import com.monkey.mcbot.bot.ai.controllers.movement.BotMovementController;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class PathfindingManager implements IPathfindingManager {

    private final Player bot;
    private final Level level;
    private final BotMovementController movementController;
    private final BotEnderpearlController enderpearlController;

    private long lastActionTime = 0;
    private Vec3 lastBotPosition;
    private int stuckCounter = 0;
    private static final long MAX_STUCK_TIME = 1500;
    private static final double MIN_MOVEMENT_THRESHOLD = 0.1;

    private long lastPathfindingAttempt = 0;
    private static final long PATHFINDING_ATTEMPT_COOLDOWN = 2200;
    private long lastStuckPearlAttempt = 0;
    private static final long STUCK_PEARL_COOLDOWN = 1200;
    private boolean usingPathfinding = false;

    public PathfindingManager(
            Player bot,
            Level level,
            BotMovementController movementController,
            BotEnderpearlController enderpearlController) {
        this.bot = bot;
        this.level = level;
        this.movementController = movementController;
        this.enderpearlController = enderpearlController;
        this.lastBotPosition = bot.position();
    }

    @Override
    public void checkForStuck(Player target) {
        long currentTime = System.currentTimeMillis();
        Vec3 currentPos = bot.position();

        double movementDistance = currentPos.distanceTo(lastBotPosition);

        if (movementDistance < MIN_MOVEMENT_THRESHOLD) {
            stuckCounter++;
        } else {
            stuckCounter = 0;
            usingPathfinding = false;
            movementController.clearPath();
        }

        if (stuckCounter > 30 || (currentTime - lastActionTime > MAX_STUCK_TIME)) {
            if (!usingPathfinding || movementController.shouldRecalculatePath()) {
                attemptPathfindingOrPearl(target);
            } else {
                movementController.followPath();
            }

            stuckCounter = 0;
            lastActionTime = currentTime;
        }
    }

    @Override
    public void attemptPathfindingOrPearl(Player target) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastPathfindingAttempt < PATHFINDING_ATTEMPT_COOLDOWN) {
            if (shouldAttemptStuckPearl(target, currentTime)) {
                if (tryUnstuckPearl(target, currentTime)) {
                    return;
                }
            }
            forceUnstuck(target);
            return;
        }

        lastPathfindingAttempt = currentTime;

        if (movementController.calculatePathTo(target.position())) {
            usingPathfinding = true;
            movementController.followPath();
            return;
        }

        if (enderpearlController.canUseEnderpearl()
                && bot.distanceTo(target) > 4.0
                && hasObstacleBetween(bot.position(), target.position())) {

            if (tryUnstuckPearl(target, currentTime)) {
                return;
            }
        }

        forceUnstuck(target);
    }

    @Override
    public boolean hasObstacleBetween(Vec3 start, Vec3 end) {
        Vec3 direction = end.subtract(start).normalize();
        double distance = start.distanceTo(end);
        int steps = (int) (distance * 2);

        for (int i = 1; i < steps; i++) {
            Vec3 checkPos = start.add(direction.scale(i * 0.5));
            BlockPos blockPos = BlockPos.containing(checkPos);

            if (!level.getBlockState(blockPos).isAir()
                    || !level.getBlockState(blockPos.above()).isAir()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Vec3 calculatePearlTargetAroundPlayer(Player target) {
        Vec3 targetPos = target.position();
        Vec3 botPos = bot.position();
        double baseAngle = Math.atan2(botPos.z - targetPos.z, botPos.x - targetPos.x);

        double[] radii = {2.8D, 3.4D, 4.1D, 4.8D, 5.4D};
        double[] angleOffsets = {0D, 25D, -25D, 50D, -50D, 80D, -80D, 110D, -110D, 180D};
        int[] yOffsets = {0, -1, 1};

        Vec3 best = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (double radius : radii) {
            for (double offsetDeg : angleOffsets) {
                double angle = baseAngle + Math.toRadians(offsetDeg);
                double x = targetPos.x + Math.cos(angle) * radius;
                double z = targetPos.z + Math.sin(angle) * radius;

                for (int yOffset : yOffsets) {
                    Vec3 potentialTarget = new Vec3(x, targetPos.y + yOffset, z);
                    BlockPos blockPos = BlockPos.containing(potentialTarget);
                    if (!isSafeLandingSpot(blockPos)) {
                        continue;
                    }

                    Vec3 center = Vec3.atCenterOf(blockPos);
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

        if (best != null) {
            return best;
        }

        return calculatePearlSideStepTarget(target);
    }

    @Override
    public boolean isSafeLandingSpot(BlockPos pos) {
        return !level.getBlockState(pos.below()).isAir()
                && level.getBlockState(pos).isAir()
                && level.getBlockState(pos.above()).isAir();
    }

    @Override
    public void forceUnstuck(Player target) {
        double distance = bot.distanceTo(target);

        if (distance <= 1.0) {
            movementController.moveAwayFrom(target, 3.0);
        } else if (distance >= 10.0) {
            movementController.moveTowards(target, 3.0);
        } else {
            Vec3 strafeDirection = getStrafeDirection(target);
            if (isFacingSolidWall()) {
                strafeDirection = strafeDirection.scale(-1.0D);
            }
            Vec3 botPos = bot.position();
            Vec3 newPos = botPos.add(strafeDirection.scale(2.0));
            movementController.moveToPosition(newPos);
        }
    }

    private Vec3 getStrafeDirection(Player target) {
        Vec3 toTarget = target.position().subtract(bot.position()).normalize();
        return new Vec3(-toTarget.z, 0, toTarget.x);
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
        this.lastBotPosition = bot.position();
    }

    public void updateLastActionTime() {
        this.lastActionTime = System.currentTimeMillis();
    }

    private boolean shouldAttemptStuckPearl(Player target, long currentTime) {
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

        return hasObstacleBetween(bot.position(), target.position()) || isFacingSolidWall() || stuckCounter > 18;
    }

    private boolean tryUnstuckPearl(Player target, long currentTime) {
        Vec3 pearlTarget = calculatePearlTargetAroundPlayer(target);
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

    private Vec3 calculatePearlSideStepTarget(Player target) {
        Vec3 botPos = bot.position();
        Vec3 toTarget = target.position().subtract(botPos);
        if (toTarget.lengthSqr() < 1.0E-5D) {
            toTarget = bot.getLookAngle();
        }
        Vec3 lateral = new Vec3(-toTarget.z, 0.0D, toTarget.x).normalize();
        if (lateral.lengthSqr() < 1.0E-5D) {
            return null;
        }

        double[] scales = {3.2D, 4.0D, 4.8D};
        for (double scale : scales) {
            for (int sign : new int[] {1, -1}) {
                Vec3 candidate = botPos.add(lateral.scale(scale * sign)).add(0.0D, 0.6D, 0.0D);
                BlockPos blockPos = BlockPos.containing(candidate);
                if (!isSafeLandingSpot(blockPos)) {
                    continue;
                }
                Vec3 center = Vec3.atCenterOf(blockPos);
                if (hasThrowPath(center)) {
                    return center;
                }
            }
        }
        return null;
    }

    private double evaluatePearlTargetScore(Vec3 candidate, Vec3 targetPos, Vec3 botPos) {
        double distToTarget = candidate.distanceTo(targetPos);
        double distToBot = candidate.distanceTo(botPos);
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
        Vec3 eyes = bot.getEyePosition(1.0F);
        Vec3 look = bot.getLookAngle();
        if (look.lengthSqr() < 1.0E-5D) {
            return false;
        }
        look = look.normalize();

        for (double step = 0.8D; step <= 1.8D; step += 0.5D) {
            Vec3 check = eyes.add(look.scale(step));
            BlockPos blockPos = BlockPos.containing(check);
            if (level.getBlockState(blockPos).isSolidRender()
                    || level.getBlockState(blockPos.above()).isSolidRender()) {
                return true;
            }
        }
        return false;
    }

    private boolean hasThrowPath(Vec3 destination) {
        Vec3 eyes = bot.getEyePosition(1.0F);
        net.minecraft.world.level.ClipContext context = new net.minecraft.world.level.ClipContext(
                eyes,
                destination,
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE,
                bot);
        HitResult result = level.clip(context);
        if (result.getType() == HitResult.Type.MISS) {
            return true;
        }

        if (result instanceof BlockHitResult blockHit) {
            BlockPos destinationBlock = BlockPos.containing(destination);
            BlockPos hitBlock = blockHit.getBlockPos();
            return hitBlock.equals(destinationBlock)
                    || hitBlock.equals(destinationBlock.below())
                    || hitBlock.equals(destinationBlock.above());
        }

        return false;
    }
}
