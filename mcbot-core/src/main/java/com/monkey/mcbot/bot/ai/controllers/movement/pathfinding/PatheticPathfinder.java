package com.monkey.mcbot.bot.ai.controllers.movement.pathfinding;

import com.monkey.mcbot.bot.ai.controllers.movement.helper.interf.IPathfinder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class PatheticPathfinder implements IPathfinder {
    private static final double MAX_LOCAL_PATH_DISTANCE = 48.0D;
    private static final double OBSTACLE_LOOKAHEAD_DISTANCE = 16.0D;
    private static final double NEAR_TARGET_RECALCULATION_DISTANCE_SQUARED = 4.0D;
    private static final double FAR_TARGET_RECALCULATION_DISTANCE_SQUARED = 16.0D;
    private static final double FAR_TARGET_DISTANCE_SQUARED = 400.0D;
    private static final long MIN_RECALCULATION_INTERVAL_MS = 1_000L;
    private static final long MAX_PATH_AGE_MS = 8_000L;
    private static final long WAYPOINT_STALL_TIMEOUT_MS = 800L;
    private static final long FAILED_WAYPOINT_AVOIDANCE_MS = 5_000L;
    private static final int MAX_SAFE_WAYPOINT_LOOKAHEAD = 5;
    private static final double MEANINGFUL_PROGRESS_DISTANCE = 0.08D;
    private static final double PATH_POINT_HORIZONTAL_TOLERANCE_SQUARED = 0.35D * 0.35D;
    private static final double PATH_POINT_VERTICAL_TOLERANCE = 0.8D;

    private final Player bot;
    private final BotTraversalEnvironment environment;
    private final PatheticPathPlanner pathPlanner;
    private final PathGoalResolver goalResolver;

    private List<Vec3> currentPath = List.of();
    private int pathIndex;
    private long lastPathCalculation;
    private Vec3 lastRequestedTarget;
    private int observedPathIndex = -1;
    private double bestWaypointDistance = Double.POSITIVE_INFINITY;
    private long lastWaypointProgressTime;
    private boolean waypointStalled;
    private boolean waypointSelectionRequired = true;
    private final Map<BlockPos, Long> avoidedWaypoints = new HashMap<>();

    public PatheticPathfinder(Player bot, BotTraversalEnvironment environment) {
        this.bot = Objects.requireNonNull(bot, "bot");
        this.environment = Objects.requireNonNull(environment, "environment");
        this.pathPlanner = new PatheticPathPlanner(environment);
        this.goalResolver = new PathGoalResolver(environment);
    }

    @Override
    public boolean calculatePathTo(Vec3 targetPos) {
        Objects.requireNonNull(targetPos, "targetPos");
        Vec3 startPos = bot.position();
        lastRequestedTarget = targetPos;
        lastPathCalculation = System.currentTimeMillis();

        BlockPos start = BlockPos.containing(startPos);
        BlockPos requestedGoal = BlockPos.containing(limitToLocalGoal(startPos, targetPos));
        removeExpiredAvoidances(lastPathCalculation);
        Set<BlockPos> excluded = Set.copyOf(avoidedWaypoints.keySet());
        BlockPos goal = goalResolver.resolve(start, requestedGoal, candidate -> !excluded.contains(candidate));
        if (goal == null) {
            clearPath();
            return false;
        }

        List<BlockPos> blockPath = pathPlanner.findPath(start, goal, excluded);
        if (blockPath.isEmpty()) {
            clearPath();
            return false;
        }

        List<Vec3> movementPath = new ArrayList<>(blockPath.size());
        for (BlockPos point : blockPath) {
            movementPath.add(Vec3.atBottomCenterOf(point));
        }

        currentPath = List.copyOf(movementPath);
        pathIndex = currentPath.size() > 1 ? 1 : 0;
        resetWaypointProgress();
        return hasActivePath();
    }

    @Override
    public boolean followPath() {
        while (hasActivePath() && hasReached(currentPath.get(pathIndex))) {
            pathIndex++;
            resetWaypointProgress();
        }
        if (hasActivePath()) {
            if (waypointSelectionRequired) {
                selectFurthestSafeWaypoint();
            }
            updateWaypointProgress();
        }
        return hasActivePath();
    }

    @Override
    public boolean hasActivePath() {
        return pathIndex >= 0 && pathIndex < currentPath.size();
    }

    @Override
    public void clearPath() {
        currentPath = List.of();
        pathIndex = 0;
        resetWaypointProgress();
    }

    @Override
    public boolean shouldRecalculatePath() {
        return hasActivePath()
                && (waypointStalled || System.currentTimeMillis() - lastPathCalculation >= MAX_PATH_AGE_MS);
    }

    @Override
    public boolean shouldRecalculatePath(Vec3 targetPos) {
        if (!hasActivePath() || lastRequestedTarget == null) {
            return false;
        }

        long pathAge = System.currentTimeMillis() - lastPathCalculation;
        if (waypointStalled) {
            rememberFailedWaypoint(System.currentTimeMillis());
            return true;
        }
        if (pathAge < MIN_RECALCULATION_INTERVAL_MS) {
            return false;
        }

        double targetMovementThreshold = bot.position().distanceToSqr(targetPos) >= FAR_TARGET_DISTANCE_SQUARED
                ? FAR_TARGET_RECALCULATION_DISTANCE_SQUARED
                : NEAR_TARGET_RECALCULATION_DISTANCE_SQUARED;
        return pathAge >= MAX_PATH_AGE_MS
                || lastRequestedTarget.distanceToSqr(targetPos) >= targetMovementThreshold
                || !isDirectPathClear(bot.position(), currentPath.get(pathIndex));
    }

    @Override
    public boolean isPathObstructed(Vec3 targetPos) {
        Vec3 start = bot.position();
        Vec3 delta = targetPos.subtract(start);
        Vec3 lookaheadTarget = delta.lengthSqr() > OBSTACLE_LOOKAHEAD_DISTANCE * OBSTACLE_LOOKAHEAD_DISTANCE
                ? start.add(delta.normalize().scale(OBSTACLE_LOOKAHEAD_DISTANCE))
                : targetPos;
        return !isDirectPathClear(start, lookaheadTarget);
    }

    @Override
    public Vec3 getCurrentPathPoint() {
        return hasActivePath() ? currentPath.get(pathIndex) : null;
    }

    private boolean isDirectPathClear(Vec3 start, Vec3 end) {
        Vec3 delta = end.subtract(start);
        double horizontalDistance = delta.horizontalDistance();
        if (horizontalDistance < 0.25D) {
            return Math.abs(delta.y) < 0.75D;
        }

        int steps = Math.max(1, (int) Math.ceil(horizontalDistance / 0.45D));
        BlockPos previous = BlockPos.containing(start);
        for (int step = 1; step <= steps; step++) {
            double progress = (double) step / steps;
            int x = (int) Math.floor(start.x + delta.x * progress);
            int z = (int) Math.floor(start.z + delta.z * progress);
            int expectedY = (int) Math.floor(start.y + delta.y * progress);
            BlockPos next = findTraversableAt(x, expectedY, z, previous);
            if (next == null) {
                return false;
            }
            previous = next;
        }
        return true;
    }

    private BlockPos findTraversableAt(int x, int expectedY, int z, BlockPos previous) {
        int[] yOffsets = {0, 1, -1, -2, -3};
        for (int yOffset : yOffsets) {
            BlockPos candidate = new BlockPos(x, expectedY + yOffset, z);
            if (!isTemporarilyAvoided(candidate)
                    && (candidate.equals(previous) || environment.canTraverse(previous, candidate))) {
                return candidate;
            }
        }
        return null;
    }

    private static Vec3 limitToLocalGoal(Vec3 start, Vec3 target) {
        Vec3 delta = target.subtract(start);
        if (delta.lengthSqr() <= MAX_LOCAL_PATH_DISTANCE * MAX_LOCAL_PATH_DISTANCE) {
            return target;
        }
        return start.add(delta.normalize().scale(MAX_LOCAL_PATH_DISTANCE));
    }

    private boolean hasReached(Vec3 point) {
        Vec3 position = bot.position();
        double dx = position.x - point.x;
        double dz = position.z - point.z;
        return dx * dx + dz * dz <= PATH_POINT_HORIZONTAL_TOLERANCE_SQUARED
                && Math.abs(position.y - point.y) <= PATH_POINT_VERTICAL_TOLERANCE;
    }

    private void updateWaypointProgress() {
        long now = System.currentTimeMillis();
        Vec3 waypoint = currentPath.get(pathIndex);
        double distance = bot.position().distanceTo(waypoint);

        if (observedPathIndex != pathIndex) {
            observedPathIndex = pathIndex;
            bestWaypointDistance = distance;
            lastWaypointProgressTime = now;
            waypointStalled = false;
            return;
        }

        if (distance <= bestWaypointDistance - MEANINGFUL_PROGRESS_DISTANCE) {
            bestWaypointDistance = distance;
            lastWaypointProgressTime = now;
            waypointStalled = false;
            return;
        }

        if (now - lastWaypointProgressTime >= WAYPOINT_STALL_TIMEOUT_MS) {
            waypointStalled = true;
        }
    }

    private void selectFurthestSafeWaypoint() {
        Vec3 currentPosition = bot.position();
        int selectedIndex = PathWaypointSelector.furthestReachable(
                currentPath,
                pathIndex,
                MAX_SAFE_WAYPOINT_LOOKAHEAD,
                waypoint -> isDirectPathClear(currentPosition, waypoint));
        if (selectedIndex != pathIndex) {
            pathIndex = selectedIndex;
            resetWaypointProgress();
        }
        waypointSelectionRequired = false;
    }

    private void rememberFailedWaypoint(long now) {
        if (!hasActivePath()) {
            return;
        }
        avoidedWaypoints.put(BlockPos.containing(currentPath.get(pathIndex)), now + FAILED_WAYPOINT_AVOIDANCE_MS);
    }

    private void removeExpiredAvoidances(long now) {
        avoidedWaypoints.entrySet().removeIf(entry -> entry.getValue() <= now);
    }

    private boolean isTemporarilyAvoided(BlockPos position) {
        Long expiresAt = avoidedWaypoints.get(position);
        return expiresAt != null && expiresAt > System.currentTimeMillis();
    }

    private void resetWaypointProgress() {
        observedPathIndex = -1;
        bestWaypointDistance = Double.POSITIVE_INFINITY;
        lastWaypointProgressTime = System.currentTimeMillis();
        waypointStalled = false;
        waypointSelectionRequired = true;
    }
}
