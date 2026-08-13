package com.monkey.ultimatebot.bot.ai.controllers.movement.pathfinding;


import java.util.Collections;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.controllers.movement.helper.interf.IPathfinder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

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

    private final ITrainingBot bot;
    private final BotTraversalEnvironment environment;
    private final PatheticPathPlanner pathPlanner;
    private final PathGoalResolver goalResolver;

    private List<Vector> currentPath = Collections.emptyList();
    private int pathIndex;
    private long lastPathCalculation;
    private @Nullable Vector lastRequestedTarget;
    private int observedPathIndex = -1;
    private double bestWaypointDistance = Double.POSITIVE_INFINITY;
    private long lastWaypointProgressTime;
    private boolean waypointStalled;
    private boolean waypointSelectionRequired = true;
    private final Map<BlockVector, Long> avoidedWaypoints = new HashMap<>();

    public PatheticPathfinder(ITrainingBot bot, BotTraversalEnvironment environment) {
        this.bot = Objects.requireNonNull(bot, "bot");
        this.environment = Objects.requireNonNull(environment, "environment");
        this.pathPlanner = new PatheticPathPlanner(environment);
        this.goalResolver = new PathGoalResolver(environment);
    }

    @Override
    public boolean calculatePathTo(Vector targetPos) {
        Objects.requireNonNull(targetPos, "targetPos");
        Vector startPos = bot.bukkitPosition();
        lastRequestedTarget = targetPos.clone();
        lastPathCalculation = System.currentTimeMillis();

        BlockVector start = blockAt(startPos);
        BlockVector requestedGoal = blockAt(limitToLocalGoal(startPos, targetPos));
        removeExpiredAvoidances(lastPathCalculation);
        Set<BlockVector> excluded = com.monkey.ultimatebot.common.util.ImmutableCollections.copyOf(avoidedWaypoints.keySet());
        BlockVector goal = goalResolver.resolve(start, requestedGoal, candidate -> !excluded.contains(candidate));
        if (goal == null) {
            clearPath();
            return false;
        }

        List<BlockVector> blockPath = pathPlanner.findPath(start, goal, excluded);
        if (blockPath.isEmpty()) {
            clearPath();
            return false;
        }

        List<Vector> movementPath = new ArrayList<>(blockPath.size());
        for (BlockVector point : blockPath) {
            movementPath.add(bottomCenterOf(point));
        }

        currentPath = com.monkey.ultimatebot.common.util.ImmutableCollections.copyOf(movementPath);
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
        currentPath = Collections.emptyList();
        pathIndex = 0;
        resetWaypointProgress();
    }

    @Override
    public boolean shouldRecalculatePath() {
        return hasActivePath()
                && (waypointStalled || System.currentTimeMillis() - lastPathCalculation >= MAX_PATH_AGE_MS);
    }

    @Override
    public boolean shouldRecalculatePath(Vector targetPos) {
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

        double targetMovementThreshold = bot.bukkitPosition().distanceSquared(targetPos) >= FAR_TARGET_DISTANCE_SQUARED
                ? FAR_TARGET_RECALCULATION_DISTANCE_SQUARED
                : NEAR_TARGET_RECALCULATION_DISTANCE_SQUARED;
        return pathAge >= MAX_PATH_AGE_MS
                || lastRequestedTarget.distanceSquared(targetPos) >= targetMovementThreshold
                || !isDirectPathClear(bot.bukkitPosition(), currentPath.get(pathIndex));
    }

    @Override
    public boolean isPathObstructed(Vector targetPos) {
        Vector start = bot.bukkitPosition();
        Vector delta = targetPos.clone().subtract(start);
        Vector lookaheadTarget = delta.lengthSquared() > OBSTACLE_LOOKAHEAD_DISTANCE * OBSTACLE_LOOKAHEAD_DISTANCE
                ? start.clone().add(delta.normalize().multiply(OBSTACLE_LOOKAHEAD_DISTANCE))
                : targetPos;
        return !isDirectPathClear(start, lookaheadTarget);
    }

    @Override
    public @Nullable Vector getCurrentPathPoint() {
        return hasActivePath() ? currentPath.get(pathIndex) : null;
    }

    private boolean isDirectPathClear(Vector start, Vector end) {
        Vector delta = end.clone().subtract(start);
        double horizontalDistance = horizontalLength(delta);
        if (horizontalDistance < 0.25D) {
            return Math.abs(delta.getY()) < 0.75D;
        }

        int steps = Math.max(1, (int) Math.ceil(horizontalDistance / 0.45D));
        BlockVector previous = blockAt(start);
        for (int step = 1; step <= steps; step++) {
            double progress = (double) step / steps;
            int x = (int) Math.floor(start.getX() + delta.getX() * progress);
            int z = (int) Math.floor(start.getZ() + delta.getZ() * progress);
            int expectedY = (int) Math.floor(start.getY() + delta.getY() * progress);
            BlockVector next = findTraversableAt(x, expectedY, z, previous);
            if (next == null) {
                return false;
            }
            previous = next;
        }
        return true;
    }

    private @Nullable BlockVector findTraversableAt(int x, int expectedY, int z, BlockVector previous) {
        int[] yOffsets = {0, 1, -1, -2, -3};
        for (int yOffset : yOffsets) {
            BlockVector candidate = new BlockVector(x, expectedY + yOffset, z);
            if (!isTemporarilyAvoided(candidate)
                    && (candidate.equals(previous) || environment.canTraverse(previous, candidate))) {
                return candidate;
            }
        }
        return null;
    }

    private static Vector limitToLocalGoal(Vector start, Vector target) {
        Vector delta = target.clone().subtract(start);
        if (delta.lengthSquared() <= MAX_LOCAL_PATH_DISTANCE * MAX_LOCAL_PATH_DISTANCE) {
            return target;
        }
        return start.clone().add(delta.normalize().multiply(MAX_LOCAL_PATH_DISTANCE));
    }

    private boolean hasReached(Vector point) {
        Vector position = bot.bukkitPosition();
        double dx = position.getX() - point.getX();
        double dz = position.getZ() - point.getZ();
        return dx * dx + dz * dz <= PATH_POINT_HORIZONTAL_TOLERANCE_SQUARED
                && Math.abs(position.getY() - point.getY()) <= PATH_POINT_VERTICAL_TOLERANCE;
    }

    private void updateWaypointProgress() {
        long now = System.currentTimeMillis();
        Vector waypoint = currentPath.get(pathIndex);
        double distance = bot.bukkitPosition().distance(waypoint);

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
        Vector currentPosition = bot.bukkitPosition();
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
        avoidedWaypoints.put(blockAt(currentPath.get(pathIndex)), now + FAILED_WAYPOINT_AVOIDANCE_MS);
    }

    private void removeExpiredAvoidances(long now) {
        avoidedWaypoints.entrySet().removeIf(entry -> entry.getValue() <= now);
    }

    private boolean isTemporarilyAvoided(BlockVector position) {
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

    private static BlockVector blockAt(Vector position) {
        return new BlockVector(
                (int) Math.floor(position.getX()), (int) Math.floor(position.getY()), (int) Math.floor(position.getZ()));
    }

    private static Vector bottomCenterOf(BlockVector position) {
        return new Vector(position.getBlockX() + 0.5D, position.getBlockY(), position.getBlockZ() + 0.5D);
    }

    private static double horizontalLength(Vector vector) {
        return Math.hypot(vector.getX(), vector.getZ());
    }
}
