package it.coralmc.sandbox.bot.ai.controllers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class BotMovementController {
    private static final long MIN_PATTERN_DURATION = 1500;
    private static final int ZIG_ZAG_CHANGE_TICKS = 12;
    private static final long PATH_RECALCULATION_COOLDOWN = 2000;
    private static final long CACHE_CLEAN_INTERVAL = 5000;
    private static final int MAX_CACHE_SIZE = 1000;
    private static final int MAX_PATHFINDING_ITERATIONS = 200;
    private static final int MAX_PATHFINDING_TIME_MS = 20;
    private static final double MAX_PATHFINDING_DISTANCE = 30.0;
    private static final double PATH_STEP_SIZE = 1.0;
    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final long FAILED_ATTEMPT_COOLDOWN = 1000;
    private final Player bot;
    private final Level level;
    private final Map<BlockPos, Boolean> blockStateCache = new HashMap<>();
    private MovementPattern currentPattern = MovementPattern.DIRECT;
    private double[] diversionDirection = null;
    private int diversionTicks = 0;
    private long lastPatternChange = 0;
    private double movementSpeed = 0.25;
    private double jumpVelocity = 0.42;
    private double currentTargetDistance = 3.0;
    private double strafeAngle = 0.0;
    private boolean strafeClockwise = true;
    private int zigZagDirection = 1;
    private int zigZagCounter = 0;
    private Vec3 lastBotPosition;
    private Vec3 lastTargetPosition;
    private Vec3 targetVelocity = Vec3.ZERO;
    private boolean isUnderFire = false;
    private long lastDamageTime = 0;
    private int consecutiveHits = 0;
    private boolean preferHighGround = false;
    private boolean avoidCorners = true;
    private Vec3 lastSafePosition = null;
    private List<Vec3> currentPath = new ArrayList<>();
    private int pathIndex = 0;
    private long lastPathRecalculation = 0;
    private long lastCacheClean = 0;
    private int failedPathfindingAttempts = 0;
    private long lastFailedAttemptTime = 0;
    public BotMovementController(Player bot, Level level) {
        this.bot = bot;
        this.level = level;
        this.lastBotPosition = bot.position();
    }

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

    private boolean isPositionPassableCached(BlockPos pos) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCacheClean > CACHE_CLEAN_INTERVAL) {
            cleanCache();
            lastCacheClean = currentTime;
        }

        Boolean cached = blockStateCache.get(pos);
        if (cached != null) {
            return cached;
        }

        if (blockStateCache.size() >= MAX_CACHE_SIZE) {
            blockStateCache.clear();
        }

        boolean result = isPositionPassable(pos);
        blockStateCache.put(pos, result);
        return result;
    }

    private void cleanCache() {
        if (blockStateCache.size() > MAX_CACHE_SIZE / 2) {
            blockStateCache.clear();
        }
    }

    private boolean isPositionPassable(BlockPos pos) {
        try {
            if (pos.getY() < -64 || pos.getY() > 319) {
                return false;
            }

            BlockState current = level.getBlockState(pos);
            BlockState above = level.getBlockState(pos.above());
            BlockState below = level.getBlockState(pos.below());

            boolean canStandOn = !below.isAir();
            boolean canPassThrough = current.isAir() && above.isAir();

            return canPassThrough && canStandOn;
        } catch (Exception e) {
            return false;
        }
    }

    private double estimateDistance(BlockPos a, BlockPos b) {
        return Math.sqrt(a.distSqr(b));
    }

    private void reconstructPath(JpsNode endNode) {
        JpsNode current = endNode;
        int pathLength = 0;

        while (current != null && pathLength < 100) {
            currentPath.add(0, Vec3.atCenterOf(current.position));
            current = current.cameFrom;
            pathLength++;
        }
    }

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

        moveToPosition(targetPoint);
        return true;
    }

    public boolean hasActivePath() {
        return !currentPath.isEmpty() && pathIndex < currentPath.size();
    }

    public void clearPath() {
        currentPath.clear();
        pathIndex = 0;
    }

    public boolean shouldRecalculatePath() {
        return System.currentTimeMillis() - lastPathRecalculation > PATH_RECALCULATION_COOLDOWN;
    }

    public void ensureMovement() {
        if (bot.getDeltaMovement().horizontalDistance() < 0.05 && bot.onGround()) {
            double randomAngle = Math.random() * 2 * Math.PI;
            double smallMoveX = Math.cos(randomAngle) * movementSpeed * 0.3;
            double smallMoveZ = Math.sin(randomAngle) * movementSpeed * 0.3;
            bot.setDeltaMovement(smallMoveX, bot.getDeltaMovement().y, smallMoveZ);
        }
    }

    private boolean calculateJpsPath(Vec3 startPos, Vec3 targetPos, long startTime) {
        currentPath.clear();
        pathIndex = 0;

        BlockPos startBlock = BlockPos.containing(startPos);
        BlockPos targetBlock = BlockPos.containing(targetPos);

        if (!isPositionPassableCached(targetBlock)) {
            targetBlock = findNearestReachablePosition(targetBlock, startBlock);
            if (targetBlock == null) {
                failedPathfindingAttempts++;
                lastFailedAttemptTime = System.currentTimeMillis();
                return false;
            }
        }

        Set<BlockPos> closedSet = new HashSet<>();
        PriorityQueue<JpsNode> openSet = new PriorityQueue<>();
        Map<BlockPos, JpsNode> allNodes = new HashMap<>();

        JpsNode startNode = new JpsNode(startBlock, null, 0, estimateDistance(startBlock, targetBlock));
        openSet.add(startNode);
        allNodes.put(startBlock, startNode);

        int iterations = 0;
        boolean foundPath = false;

        while (!openSet.isEmpty() && iterations < MAX_PATHFINDING_ITERATIONS) {
            if (System.currentTimeMillis() - startTime > MAX_PATHFINDING_TIME_MS) {
                break;
            }

            iterations++;
            JpsNode currentNode = openSet.poll();

            if (currentNode.position.distSqr(targetBlock) < 4) {
                reconstructPath(currentNode);
                foundPath = true;
                break;
            }

            closedSet.add(currentNode.position);

            for (Direction direction : Direction.values()) {
                if (direction == Direction.UP || direction == Direction.DOWN) {
                    BlockPos nextPos = currentNode.position.relative(direction);
                    if (isPositionPassableCached(nextPos) && !closedSet.contains(nextPos)) {
                        double cost = currentNode.gScore + 1;
                        JpsNode neighborNode = allNodes.get(nextPos);

                        if (neighborNode == null) {
                            neighborNode = new JpsNode(nextPos, currentNode, cost,
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
                        JpsNode neighborNode = allNodes.get(jumpPoint);

                        if (neighborNode == null) {
                            neighborNode = new JpsNode(jumpPoint, currentNode, cost,
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

    private void executeCrystalSpamMovement(Player target, double targetDistance) {
        Vec3 targetPos = target.position();
        Vec3 botPos = bot.position();
        double yDiff = botPos.y - targetPos.y;

        if (yDiff > -2.0) {
            Vec3 belowTarget = new Vec3(targetPos.x, targetPos.y - 3, targetPos.z);
            Vec3 direction = belowTarget.subtract(botPos).normalize();
            double moveX = direction.x * movementSpeed * 1.3;
            double moveZ = direction.z * movementSpeed * 1.3;
            bot.setDeltaMovement(moveX, bot.getDeltaMovement().y, moveZ);
        } else {
            executeStrafeCircle(target, targetDistance);
        }
    }

    public void moveTowards(Player target, double targetDistance) {
        updateMovementData(target);
        this.currentTargetDistance = targetDistance;
        selectOptimalMovementPattern(target, targetDistance);
        executeMovementPattern(target, targetDistance);
    }

    public void moveToTarget(Player target, double targetDistance) {
        double currentDistance = bot.distanceTo(target);
        if (Math.abs(currentDistance - targetDistance) <= 0.3) {
            if (currentPattern != MovementPattern.STRAFE_CIRCLE) {
                setMovementPattern(MovementPattern.STRAFE_CIRCLE);
            }
            executeStrafeCircle(target, targetDistance);
        } else if (currentDistance < targetDistance) {
            moveAwayFrom(target, targetDistance);
        } else {
            moveTowards(target, targetDistance);
        }
    }

    public void moveAwayFrom(Player target, double targetDistance) {
        updateMovementData(target);
        this.currentTargetDistance = targetDistance;
        if (currentPattern != MovementPattern.RETREAT_SPIRAL &&
                currentPattern != MovementPattern.EVASIVE_ZIG_ZAG) {
            setMovementPattern(MovementPattern.RETREAT_SPIRAL);
        }
        executeRetreatMovement(target, targetDistance);
    }

    private void updateMovementData(Player target) {
        Vec3 currentTargetPos = target.position();
        Vec3 currentBotPos = bot.position();

        if (lastTargetPosition != null) {
            targetVelocity = currentTargetPos.subtract(lastTargetPosition);
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastDamageTime > 3000) {
            isUnderFire = false;
            consecutiveHits = 0;
        }

        lastTargetPosition = currentTargetPos;
        lastBotPosition = currentBotPos;
    }

    private void selectOptimalMovementPattern(Player target, double targetDistance) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastPatternChange < MIN_PATTERN_DURATION) {
            return;
        }

        double distance = bot.distanceTo(target);
        Vec3 botPos = bot.position();
        Vec3 targetPos = target.position();
        double yDiff = botPos.y - targetPos.y;

        MovementPattern newPattern;
        if (isUnderFire || consecutiveHits >= 2) {
            newPattern = MovementPattern.EVASIVE_ZIG_ZAG;
        } else if (distance < 4.0 && targetDistance < 4.0) {
            newPattern = MovementPattern.STRAFE_CIRCLE;
        } else if (distance >= 4.0 && distance <= 8.0) {
            if (hasComplexTerrain(botPos)) {
                newPattern = MovementPattern.TERRAIN_ADAPTIVE;
            } else {
                newPattern = Math.random() < 0.6 ? MovementPattern.STRAFE_FIGURE8 : MovementPattern.STRAFE_CIRCLE;
            }
        } else if (distance > 8.0) {
            if (yDiff < -2.0 || hasObstacles(botPos, targetPos)) {
                newPattern = MovementPattern.TERRAIN_ADAPTIVE;
            } else {
                newPattern = MovementPattern.DIRECT;
            }
        } else {
            newPattern = MovementPattern.DIRECT;
        }

        setMovementPattern(newPattern);
    }

    private void setMovementPattern(MovementPattern pattern) {
        if (pattern != currentPattern) {
            currentPattern = pattern;
            lastPatternChange = System.currentTimeMillis();
            switch (pattern) {
                case STRAFE_CIRCLE -> {
                    strafeClockwise = Math.random() < 0.5;
                    strafeAngle = Math.random() * 2 * Math.PI;
                }
                case EVASIVE_ZIG_ZAG -> {
                    zigZagDirection = Math.random() < 0.5 ? 1 : -1;
                    zigZagCounter = 0;
                }
                case RETREAT_SPIRAL -> {
                    strafeAngle = 0;
                    strafeClockwise = Math.random() < 0.5;
                }
            }
        }
    }

    private void executeMovementPattern(Player target, double targetDistance) {
        switch (currentPattern) {
            case DIRECT -> executeDirectMovement(target, targetDistance);
            case STRAFE_CIRCLE -> executeStrafeCircle(target, targetDistance);
            case STRAFE_FIGURE8 -> executeStrafeFigure8(target, targetDistance);
            case EVASIVE_ZIG_ZAG -> executeEvasiveZigZag(target, targetDistance);
            case TERRAIN_ADAPTIVE -> executeTerrainAdaptive(target, targetDistance);
            case RETREAT_SPIRAL -> executeRetreatSpiral(target, targetDistance);
        }
    }

    private void executeDirectMovement(Player target, double targetDistance) {
        double targetX = target.getX();
        double targetZ = target.getZ();
        double botX = bot.getX();
        double botZ = bot.getZ();

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

        if (handleObstacles(dx, dz, botX, bot.getY(), botZ, moveX, moveZ)) {
            return;
        }

        bot.setDeltaMovement(moveX, bot.getDeltaMovement().y, moveZ);
    }

    private void executeStrafeCircle(Player target, double targetDistance) {
        Vec3 targetPos = target.position();
        Vec3 botPos = bot.position();
        double currentDistance = botPos.distanceTo(targetPos);

        double angleSpeed = 0.08 + (Math.random() * 0.04);
        strafeAngle += strafeClockwise ? angleSpeed : -angleSpeed;

        double desiredX = targetPos.x + Math.cos(strafeAngle) * targetDistance;
        double desiredZ = targetPos.z + Math.sin(strafeAngle) * targetDistance;
        Vec3 desiredPos = new Vec3(desiredX, botPos.y, desiredZ);

        Vec3 direction = desiredPos.subtract(botPos).normalize();
        double moveX = direction.x * movementSpeed * 1.1;
        double moveZ = direction.z * movementSpeed * 1.1;

        if (Math.random() < 0.01) {
            strafeClockwise = !strafeClockwise;
        }

        if (!handleObstacles(direction.x, direction.z, botPos.x, botPos.y, botPos.z, moveX, moveZ)) {
            bot.setDeltaMovement(moveX, bot.getDeltaMovement().y, moveZ);
        }
    }

    private boolean hasForcedNeighbor(BlockPos pos, Direction direction, Set<BlockPos> closedSet) {
        Direction left = direction.getCounterClockWise();
        Direction right = direction.getClockWise();

        BlockPos frontLeft = pos.relative(direction).relative(left);
        BlockPos frontRight = pos.relative(direction).relative(right);

        boolean leftBlocked = !isPositionPassableCached(pos.relative(left));
        boolean rightBlocked = !isPositionPassableCached(pos.relative(right));

        boolean frontLeftPassable = isPositionPassableCached(frontLeft);
        boolean frontRightPassable = isPositionPassableCached(frontRight);

        if (leftBlocked && frontLeftPassable && !closedSet.contains(frontLeft)) {
            return true;
        }

        if (rightBlocked && frontRightPassable && !closedSet.contains(frontRight)) {
            return true;
        }

        return false;
    }

    private void executeStrafeFigure8(Player target, double targetDistance) {
        Vec3 targetPos = target.position();
        Vec3 botPos = bot.position();

        strafeAngle += 0.12;
        double radiusX = targetDistance * 0.8;
        double radiusZ = targetDistance * 1.2;

        double desiredX = targetPos.x + Math.cos(strafeAngle) * radiusX;
        double desiredZ = targetPos.z + Math.sin(strafeAngle * 2) * radiusZ;

        Vec3 desiredPos = new Vec3(desiredX, botPos.y, desiredZ);
        Vec3 direction = desiredPos.subtract(botPos).normalize();

        double moveX = direction.x * movementSpeed * 1.05;
        double moveZ = direction.z * movementSpeed * 1.05;

        if (!handleObstacles(direction.x, direction.z, botPos.x, botPos.y, botPos.z, moveX, moveZ)) {
            bot.setDeltaMovement(moveX, bot.getDeltaMovement().y, moveZ);
        }
    }

    private void executeEvasiveZigZag(Player target, double targetDistance) {
        Vec3 targetPos = target.position();
        Vec3 botPos = bot.position();

        zigZagCounter++;
        if (zigZagCounter >= ZIG_ZAG_CHANGE_TICKS) {
            zigZagDirection *= -1;
            zigZagCounter = 0;
            if (Math.random() < 0.3) {
                zigZagDirection *= Math.random() < 0.5 ? 1 : -1;
            }
        }

        Vec3 toTarget = targetPos.subtract(botPos);
        double distanceToTarget = toTarget.length();
        if (distanceToTarget == 0) return;

        Vec3 baseDirection = toTarget.normalize();

        if (distanceToTarget > targetDistance) {
            Vec3 perpendicular = new Vec3(-baseDirection.z, 0, baseDirection.x);
            Vec3 zigzagDirection = baseDirection.add(perpendicular.scale(zigZagDirection * 0.7));
            zigzagDirection = zigzagDirection.normalize();

            double moveX = zigzagDirection.x * movementSpeed * 1.2;
            double moveZ = zigzagDirection.z * movementSpeed * 1.2;

            if (!handleObstacles(zigzagDirection.x, zigzagDirection.z, botPos.x, botPos.y, botPos.z, moveX, moveZ)) {
                bot.setDeltaMovement(moveX, bot.getDeltaMovement().y, moveZ);
            }
        } else {
            Vec3 perpendicular = new Vec3(-baseDirection.z, 0, baseDirection.x);
            Vec3 strafeDir = perpendicular.scale(zigZagDirection);

            double moveX = strafeDir.x * movementSpeed;
            double moveZ = strafeDir.z * movementSpeed;

            if (!handleObstacles(strafeDir.x, strafeDir.z, botPos.x, botPos.y, botPos.z, moveX, moveZ)) {
                bot.setDeltaMovement(moveX, bot.getDeltaMovement().y, moveZ);
            }
        }
    }

    private BlockPos findJumpPoint(BlockPos current, Direction direction, BlockPos target, Set<BlockPos> closedSet) {
        BlockPos next = current.relative(direction);

        if (!isPositionPassableCached(next)) {
            return null;
        }

        if (next.distSqr(target) < 9) {
            return next;
        }

        if (hasForcedNeighbor(next, direction, closedSet)) {
            return next;
        }

        return findJumpPoint(next, direction, target, closedSet);
    }

    private void executeTerrainAdaptive(Player target, double targetDistance) {
        Vec3 targetPos = target.position();
        Vec3 botPos = bot.position();

        Vec3 bestDirection = findBestPath(botPos, targetPos, targetDistance);
        if (bestDirection != null) {
            double moveX = bestDirection.x * movementSpeed;
            double moveZ = bestDirection.z * movementSpeed;

            if (!handleObstacles(bestDirection.x, bestDirection.z, botPos.x, botPos.y, botPos.z, moveX, moveZ)) {
                bot.setDeltaMovement(moveX, bot.getDeltaMovement().y, moveZ);
            }
        } else {
            executeDirectMovement(target, targetDistance);
        }
    }

    private void executeRetreatSpiral(Player target, double targetDistance) {
        Vec3 targetPos = target.position();
        Vec3 botPos = bot.position();

        strafeAngle += strafeClockwise ? 0.15 : -0.15;

        double currentDistance = botPos.distanceTo(targetPos);
        double spiralRadius = Math.max(targetDistance, currentDistance + 1.0);
        spiralRadius += strafeAngle * 0.1;

        double desiredX = targetPos.x + Math.cos(strafeAngle) * spiralRadius;
        double desiredZ = targetPos.z + Math.sin(strafeAngle) * spiralRadius;

        Vec3 desiredPos = new Vec3(desiredX, botPos.y, desiredZ);
        Vec3 direction = desiredPos.subtract(botPos).normalize();

        double moveX = direction.x * movementSpeed * 1.15;
        double moveZ = direction.z * movementSpeed * 1.15;

        if (!handleObstacles(direction.x, direction.z, botPos.x, botPos.y, botPos.z, moveX, moveZ)) {
            bot.setDeltaMovement(moveX, bot.getDeltaMovement().y, moveZ);
        }
    }

    private void executeRetreatMovement(Player target, double targetDistance) {
        executeRetreatSpiral(target, targetDistance);
    }

    private Vec3 findBestPath(Vec3 from, Vec3 to, double targetDistance) {
        Vec3 baseDirection = to.subtract(from).normalize();
        Vec3 targetPoint = to.subtract(baseDirection.scale(targetDistance));
        Vec3 desiredDirection = targetPoint.subtract(from).normalize();

        double[] angles = {0, Math.PI / 4, -Math.PI / 4, Math.PI / 2, -Math.PI / 2};

        for (double angle : angles) {
            Vec3 testDirection = rotateDirection(desiredDirection, angle);
            if (isPathSafeOptimized(from, testDirection, 2.0)) {
                return testDirection;
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

            if (!isPositionPassableCached(blockPos)) {
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

    private Vec3 rotateDirection(Vec3 direction, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Vec3(
                direction.x * cos - direction.z * sin,
                direction.y,
                direction.x * sin + direction.z * cos
        );
    }

    private boolean isPathSafeOptimized(Vec3 from, Vec3 direction, double distance) {
        int steps = Math.min((int) (distance), 3);
        for (int i = 1; i <= steps; i++) {
            Vec3 checkPos = from.add(direction.scale(i));
            BlockPos blockPos = BlockPos.containing(checkPos);

            if (!isPositionPassableCached(blockPos)) {
                return false;
            }
        }
        return true;
    }

    private boolean hasComplexTerrain(Vec3 position) {
        BlockPos centerPos = BlockPos.containing(position);
        int solidBlocks = 0;

        BlockPos[] checkPositions = {
                centerPos.north(),
                centerPos.south(),
                centerPos.east(),
                centerPos.west()
        };

        for (BlockPos checkPos : checkPositions) {
            if (!isPositionPassableCached(checkPos)) {
                solidBlocks++;
            }
        }

        return solidBlocks >= 2;
    }

    private boolean hasObstacles(Vec3 from, Vec3 to) {
        Vec3 direction = to.subtract(from).normalize();
        double distance = from.distanceTo(to);
        int steps = Math.min((int) (distance / 3.0), 5);

        for (int i = 1; i <= steps; i++) {
            Vec3 checkPos = from.add(direction.scale(i * 3.0));
            BlockPos blockPos = BlockPos.containing(checkPos);

            if (!isPositionPassableCached(blockPos)) {
                return true;
            }
        }
        return false;
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
            if (isPositionPassableCached(checkPos) &&
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

    private boolean handleObstacles(double dx, double dz, double botX, double botY, double botZ, double moveX, double moveZ) {
        BlockPos front = BlockPos.containing(botX + dx * 2, botY, botZ + dz * 2);
        BlockPos above = front.above();
        BlockPos below = front.below();

        boolean frontBlocked = !isPositionPassableCached(front);
        boolean aboveClear = isPositionPassableCached(above);
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
            for (int sign : new int[]{1, -1}) {
                double newAngle = angle + offset * sign;
                double newDx = Math.cos(newAngle);
                double newDz = Math.sin(newAngle);

                if (isPathClearOptimized(newDx, newDz)) {
                    return new double[]{newDx, newDz};
                }
            }
        }
        return null;
    }

    private boolean isPathClearOptimized(double dx, double dz) {
        BlockPos checkPos = BlockPos.containing(bot.getX() + dx * 2, bot.getY(), bot.getZ() + dz * 2);
        return isPositionPassableCached(checkPos);
    }

    public void setUnderFire(boolean underFire) {
        this.isUnderFire = underFire;
        this.lastDamageTime = System.currentTimeMillis();
        if (underFire) {
            this.consecutiveHits++;
            if (currentPattern != MovementPattern.EVASIVE_ZIG_ZAG) {
                setMovementPattern(MovementPattern.EVASIVE_ZIG_ZAG);
            }
        }
    }

    public void onDamageReceived() {
        setUnderFire(true);
        lastSafePosition = bot.position();
    }

    public void forceMovementPattern(MovementPattern pattern) {
        setMovementPattern(pattern);
    }

    public MovementPattern getCurrentPattern() {
        return currentPattern;
    }

    public void setPreferHighGround(boolean prefer) {
        this.preferHighGround = prefer;
    }

    public void setAvoidCorners(boolean avoid) {
        this.avoidCorners = avoid;
    }

    public void setJumpVelocity(double velocity) {
        this.jumpVelocity = Math.max(0.2, Math.min(1.0, velocity));
    }

    public void stopMovement() {
        bot.setDeltaMovement(0, bot.getDeltaMovement().y, 0);
        diversionDirection = null;
        diversionTicks = 0;
        currentPattern = MovementPattern.DIRECT;
    }

    public boolean isDiverting() {
        return diversionTicks > 0 && diversionDirection != null;
    }

    public boolean isEvading() {
        return currentPattern == MovementPattern.EVASIVE_ZIG_ZAG ||
                currentPattern == MovementPattern.RETREAT_SPIRAL;
    }

    public double getMovementSpeed() {
        return movementSpeed;
    }

    public void setMovementSpeed(double speed) {
        this.movementSpeed = Math.max(0.1, Math.min(1.5, speed));
    }

    public void emergencyEvade() {
        setMovementPattern(MovementPattern.EVASIVE_ZIG_ZAG);
        setUnderFire(true);
        setMovementSpeed(movementSpeed * 1.4);
    }

    public void seekHighGround() {
        setPreferHighGround(true);
        setMovementPattern(MovementPattern.TERRAIN_ADAPTIVE);
    }

    public void beginStrafe() {
        MovementPattern strafePattern = Math.random() < 0.7 ?
                MovementPattern.STRAFE_CIRCLE : MovementPattern.STRAFE_FIGURE8;
        setMovementPattern(strafePattern);
    }

    public boolean isStrafing() {
        return currentPattern == MovementPattern.STRAFE_CIRCLE ||
                currentPattern == MovementPattern.STRAFE_FIGURE8;
    }

    public boolean isRetreating() {
        return currentPattern == MovementPattern.RETREAT_SPIRAL;
    }

    public Vec3 getTargetVelocity() {
        return targetVelocity;
    }

    public boolean hasRecentDamage() {
        return System.currentTimeMillis() - lastDamageTime < 3000;
    }

    public int getConsecutiveHits() {
        return consecutiveHits;
    }

    public void resetCombatState() {
        isUnderFire = false;
        consecutiveHits = 0;
        currentPattern = MovementPattern.DIRECT;
        setMovementSpeed(0.25);
        blockStateCache.clear();
    }

    public void moveToPosition(Vec3 targetPos) {
        Vec3 botPos = bot.position();
        double dx = targetPos.x - botPos.x;
        double dz = targetPos.z - botPos.z;
        double distance = Math.sqrt(dx * dx + dz * dz);

        if (distance < 0.1) {
            stopMovement();
            return;
        }

        dx = dx / distance;
        dz = dz / distance;

        double moveX = dx * movementSpeed;
        double moveZ = dz * movementSpeed;

        if (handleObstacles(dx, dz, botPos.x, botPos.y, botPos.z, moveX, moveZ)) {
            return;
        }

        bot.setDeltaMovement(moveX, bot.getDeltaMovement().y, moveZ);

        if (bot.onGround() && bot.getDeltaMovement().horizontalDistance() < 0.1) {
            ensureMovement();
        }
    }

    public void maintainDistance(Player target, double targetDistance) {
        double currentDistance = bot.distanceTo(target);
        if (Math.abs(currentDistance - targetDistance) <= 0.3) {
            if (currentPattern != MovementPattern.STRAFE_CIRCLE) {
                setMovementPattern(MovementPattern.STRAFE_CIRCLE);
            }
            executeStrafeCircle(target, targetDistance);
        } else if (currentDistance < targetDistance) {
            moveAwayFrom(target, targetDistance);
        } else {
            moveTowards(target, targetDistance);
        }
    }

    public Vec3 getCurrentPathPoint() {
        if (currentPath.isEmpty() || pathIndex >= currentPath.size()) {
            return null;
        }
        return currentPath.get(pathIndex);
    }

    public int getCacheSize() {
        return blockStateCache.size();
    }

    public void clearCache() {
        blockStateCache.clear();
        lastCacheClean = System.currentTimeMillis();
    }

    public void forceCacheClean() {
        if (blockStateCache.size() > 0) {
            blockStateCache.clear();
            lastCacheClean = System.currentTimeMillis();
        }
    }

    public enum MovementPattern {
        DIRECT,
        STRAFE_CIRCLE,
        STRAFE_FIGURE8,
        EVASIVE_ZIG_ZAG,
        TERRAIN_ADAPTIVE,
        RETREAT_SPIRAL,
        CRYSTAL_SPAM
    }

    private static class JpsNode implements Comparable<JpsNode> {
        public BlockPos position;
        public JpsNode cameFrom;
        public double gScore;
        public double fScore;

        public JpsNode(BlockPos position, JpsNode cameFrom, double gScore, double hScore) {
            this.position = position;
            this.cameFrom = cameFrom;
            this.gScore = gScore;
            this.fScore = gScore + hScore;
        }

        @Override
        public int compareTo(JpsNode other) {
            return Double.compare(this.fScore, other.fScore);
        }
    }
}