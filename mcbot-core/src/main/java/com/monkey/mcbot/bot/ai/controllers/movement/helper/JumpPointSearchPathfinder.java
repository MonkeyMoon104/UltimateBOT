package com.monkey.mcbot.bot.ai.controllers.movement.helper;

import com.monkey.mcbot.bot.ai.controllers.movement.helper.interf.IBlockStateValidator;
import com.monkey.mcbot.bot.ai.controllers.movement.helper.interf.IPathfinder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class JumpPointSearchPathfinder implements IPathfinder {
    private static final long PATH_RECALCULATION_COOLDOWN = 2000;
    private static final int MAX_PATHFINDING_ITERATIONS = 200;
    private static final int MAX_PATHFINDING_TIME_MS = 20;
    private static final double MAX_PATHFINDING_DISTANCE = 30.0;
    private static final double PATH_STEP_SIZE = 1.0;
    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final long FAILED_ATTEMPT_COOLDOWN = 1000;

    private final Player bot;
    private final IBlockStateValidator blockValidator;
    private List<Vec3> currentPath = new ArrayList<>();
    private int pathIndex = 0;
    private long lastPathRecalculation = 0;
    private int failedPathfindingAttempts = 0;
    private long lastFailedAttemptTime = 0;

    public JumpPointSearchPathfinder(Player bot, IBlockStateValidator blockValidator) {
        this.bot = bot;
        this.blockValidator = blockValidator;
    }

    @Override
    public boolean calculatePathTo(Vec3 targetPos) {
        if (System.currentTimeMillis() - lastFailedAttemptTime < FAILED_ATTEMPT_COOLDOWN &&
                failedPathfindingAttempts >= MAX_FAILED_ATTEMPTS) {
            return false;
        }

        long startTime = System.currentTimeMillis();
        Vec3 startPos = bot.position();

        double distanceToTarget = startPos.distanceTo(targetPos);
        if (distanceToTarget > MAX_PATHFINDING_DISTANCE) {
            failedPathfindingAttempts++;
            lastFailedAttemptTime = System.currentTimeMillis();
            return false;
        }

        if (isDirectPathClear(startPos, targetPos)) {
            currentPath.clear();
            currentPath.add(targetPos);
            pathIndex = 0;
            lastPathRecalculation = System.currentTimeMillis();
            failedPathfindingAttempts = 0;
            return true;
        }

        return calculateJpsPath(startPos, targetPos, startTime);
    }

    private double estimateDistance(BlockPos a, BlockPos b) {
        return Math.sqrt(a.distSqr(b));
    }

    private void reconstructPath(PathfindingNode endNode) {
        PathfindingNode current = endNode;
        int pathLength = 0;

        while (current != null && pathLength < 100) {
            currentPath.add(0, Vec3.atCenterOf(current.position));
            current = current.cameFrom;
            pathLength++;
        }
    }

    @Override
    public boolean followPath() {
        if (currentPath.isEmpty() || pathIndex >= currentPath.size()) {
            return false;
        }

        Vec3 targetPoint = currentPath.get(pathIndex);
        double distanceToPoint = bot.position().distanceTo(targetPoint);

        if (distanceToPoint < 2.0 ||
                (distanceToPoint < 3.0 && Math.abs(bot.getY() - targetPoint.y) < 1.5)) {
            pathIndex++;
            if (pathIndex >= currentPath.size()) {
                return false;
            }
            targetPoint = currentPath.get(pathIndex);
        }

        return true;
    }

    @Override
    public boolean hasActivePath() {
        return !currentPath.isEmpty() && pathIndex < currentPath.size();
    }

    @Override
    public void clearPath() {
        currentPath.clear();
        pathIndex = 0;
    }

    @Override
    public boolean shouldRecalculatePath() {
        return System.currentTimeMillis() - lastPathRecalculation > PATH_RECALCULATION_COOLDOWN;
    }

    private boolean calculateJpsPath(Vec3 startPos, Vec3 targetPos, long startTime) {
        currentPath.clear();
        pathIndex = 0;

        BlockPos startBlock = BlockPos.containing(startPos);
        BlockPos targetBlock = BlockPos.containing(targetPos);

        if (!blockValidator.isPositionPassableCached(targetBlock)) {
            targetBlock = findNearestReachablePosition(targetBlock, startBlock);
            if (targetBlock == null) {
                failedPathfindingAttempts++;
                lastFailedAttemptTime = System.currentTimeMillis();
                return false;
            }
        }

        Set<BlockPos> closedSet = new HashSet<>();
        PriorityQueue<PathfindingNode> openSet = new PriorityQueue<>();
        Map<BlockPos, PathfindingNode> allNodes = new HashMap<>();

        PathfindingNode startNode = new PathfindingNode(startBlock, null, 0, estimateDistance(startBlock, targetBlock));
        openSet.add(startNode);
        allNodes.put(startBlock, startNode);

        int iterations = 0;
        boolean foundPath = false;

        while (!openSet.isEmpty() && iterations < MAX_PATHFINDING_ITERATIONS) {
            if (System.currentTimeMillis() - startTime > MAX_PATHFINDING_TIME_MS) {
                break;
            }

            iterations++;
            PathfindingNode currentNode = openSet.poll();

            if (currentNode.position.distSqr(targetBlock) < 4) {
                reconstructPath(currentNode);
                foundPath = true;
                break;
            }

            closedSet.add(currentNode.position);

            for (Direction direction : Direction.values()) {
                if (direction == Direction.UP || direction == Direction.DOWN) {
                    BlockPos nextPos = currentNode.position.relative(direction);
                    if (blockValidator.isPositionPassableCached(nextPos) && !closedSet.contains(nextPos)) {
                        double cost = currentNode.gScore + 1;
                        PathfindingNode neighborNode = allNodes.get(nextPos);

                        if (neighborNode == null) {
                            neighborNode = new PathfindingNode(nextPos, currentNode, cost,
                                    estimateDistance(nextPos, targetBlock));
                            allNodes.put(nextPos, neighborNode);
                            openSet.add(neighborNode);
                        } else if (cost < neighborNode.gScore) {
                            neighborNode.cameFrom = currentNode;
                            neighborNode.gScore = cost;
                            neighborNode.fScore = cost + estimateDistance(nextPos, targetBlock);
                            openSet.remove(neighborNode);
                            openSet.add(neighborNode);
                        }
                    }
                } else {
                    BlockPos jumpPoint = findJumpPoint(currentNode.position, direction, targetBlock, closedSet);
                    if (jumpPoint != null && !closedSet.contains(jumpPoint)) {
                        double cost = currentNode.gScore + currentNode.position.distSqr(jumpPoint);
                        PathfindingNode neighborNode = allNodes.get(jumpPoint);

                        if (neighborNode == null) {
                            neighborNode = new PathfindingNode(jumpPoint, currentNode, cost,
                                    estimateDistance(jumpPoint, targetBlock));
                            allNodes.put(jumpPoint, neighborNode);
                            openSet.add(neighborNode);
                        } else if (cost < neighborNode.gScore) {
                            neighborNode.cameFrom = currentNode;
                            neighborNode.gScore = cost;
                            neighborNode.fScore = cost + estimateDistance(jumpPoint, targetBlock);
                            openSet.remove(neighborNode);
                            openSet.add(neighborNode);
                        }
                    }
                }
            }
        }

        if (foundPath) {
            lastPathRecalculation = System.currentTimeMillis();
            failedPathfindingAttempts = 0;
            optimizePath();
            return true;
        } else {
            failedPathfindingAttempts++;
            lastFailedAttemptTime = System.currentTimeMillis();
            return false;
        }
    }

    private boolean hasForcedNeighbor(BlockPos pos, Direction direction, Set<BlockPos> closedSet) {
        Direction left = direction.getCounterClockWise();
        Direction right = direction.getClockWise();

        BlockPos frontLeft = pos.relative(direction).relative(left);
        BlockPos frontRight = pos.relative(direction).relative(right);

        boolean leftBlocked = !blockValidator.isPositionPassableCached(pos.relative(left));
        boolean rightBlocked = !blockValidator.isPositionPassableCached(pos.relative(right));

        boolean frontLeftPassable = blockValidator.isPositionPassableCached(frontLeft);
        boolean frontRightPassable = blockValidator.isPositionPassableCached(frontRight);

        if (leftBlocked && frontLeftPassable && !closedSet.contains(frontLeft)) {
            return true;
        }

        if (rightBlocked && frontRightPassable && !closedSet.contains(frontRight)) {
            return true;
        }

        return false;
    }

    private BlockPos findJumpPoint(BlockPos current, Direction direction, BlockPos target, Set<BlockPos> closedSet) {
        BlockPos searchPos = current;
        int iterations = 0;
        final int MAX_ITERATIONS = 50;

        while (iterations < MAX_ITERATIONS) {
            searchPos = searchPos.relative(direction);
            iterations++;

            if (Math.abs(searchPos.getX()) > 30000000 || Math.abs(searchPos.getZ()) > 30000000 ||
                    searchPos.getY() < -64 || searchPos.getY() > 320) {
                return null;
            }

            if (!blockValidator.isPositionPassableCached(searchPos)) {
                return null;
            }

            if (closedSet.contains(searchPos)) {
                return null;
            }

            if (searchPos.distSqr(target) < 9) {
                return searchPos;
            }

            if (hasForcedNeighbor(searchPos, direction, closedSet)) {
                return searchPos;
            }

            if (current.distSqr(searchPos) > 100 * 100) {
                return null;
            }
        }

        return null;
    }

    private boolean isDirectPathClear(Vec3 start, Vec3 end) {
        double distance = start.distanceTo(end);
        int steps = (int) (distance / PATH_STEP_SIZE);

        if (steps == 0) return true;

        Vec3 direction = end.subtract(start).normalize().scale(PATH_STEP_SIZE);
        Vec3 current = start;

        for (int i = 0; i < steps; i++) {
            current = current.add(direction);
            BlockPos blockPos = BlockPos.containing(current);

            if (!blockValidator.isPositionPassableCached(blockPos)) {
                return false;
            }
        }

        return true;
    }

    private void optimizePath() {
        if (currentPath.size() < 3) return;

        List<Vec3> optimizedPath = new ArrayList<>();
        optimizedPath.add(currentPath.get(0));

        int currentIndex = 0;
        while (currentIndex < currentPath.size() - 1) {
            int nextIndex = currentIndex + 2;

            while (nextIndex < currentPath.size()) {
                if (!isDirectPathClear(currentPath.get(currentIndex), currentPath.get(nextIndex))) {
                    nextIndex--;
                    break;
                }
                nextIndex++;
            }

            nextIndex = Math.min(nextIndex, currentPath.size() - 1);
            optimizedPath.add(currentPath.get(nextIndex));
            currentIndex = nextIndex;
        }

        currentPath = optimizedPath;
    }

    private BlockPos findNearestReachablePosition(BlockPos target, BlockPos start) {
        int minRadius = 3;
        int maxRadius = 5;

        for (int radius = minRadius; radius <= maxRadius; radius++) {
            BlockPos foundPos = findValidPositionInRing(target, start, radius);
            if (foundPos != null) {
                return foundPos;
            }
        }
        return null;
    }

    private BlockPos findValidPositionInRing(BlockPos target, BlockPos start, int radius) {
        int x = -radius;
        int z = -radius;

        for (int i = 0; i < 8 * radius; i++) {
            BlockPos checkPos = target.offset(x, 0, z);
            if (blockValidator.isPositionPassableCached(checkPos) &&
                    isDirectPathClear(Vec3.atCenterOf(start), Vec3.atCenterOf(checkPos))) {
                return checkPos;
            }

            if (x == radius && z > -radius) {
                z--;
            } else if (z == -radius && x < radius) {
                x++;
            } else if (x == -radius && z < radius) {
                z++;
            } else if (z == radius && x > -radius) {
                x--;
            }
        }
        return null;
    }

    @Override
    public Vec3 getCurrentPathPoint() {
        if (currentPath.isEmpty() || pathIndex >= currentPath.size()) {
            return null;
        }
        return currentPath.get(pathIndex);
    }
}