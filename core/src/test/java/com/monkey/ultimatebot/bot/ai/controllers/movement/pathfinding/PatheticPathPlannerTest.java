package com.monkey.ultimatebot.bot.ai.controllers.movement.pathfinding;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.bukkit.util.BlockVector;
import org.junit.jupiter.api.Test;

class PatheticPathPlannerTest {
    @Test
    void routesAroundWideObstacleInsteadOfWalkingIntoIt() {
        GridEnvironment environment = new GridEnvironment(-12, 12);
        for (int x = 2; x <= 5; x++) {
            for (int z = -1; z <= 1; z++) {
                environment.block(x, z);
            }
        }

        List<BlockVector> path = new PatheticPathPlanner(environment)
                .findPath(new BlockVector(0, 0, 0), new BlockVector(8, 0, 0));

        assertThat(path).isNotEmpty();
        assertThat(path.get(0)).isEqualTo(new BlockVector(0, 0, 0));
        assertThat(path.get(path.size() - 1)).isEqualTo(new BlockVector(8, 0, 0));
        assertThat(path).noneMatch(environment::isBlocked);
        assertThat(path).anyMatch(position -> Math.abs(position.getBlockZ()) >= 2);
    }

    @Test
    void doesNotCutDiagonallyThroughBlockedCorners() {
        GridEnvironment environment = new GridEnvironment(-8, 8);
        environment.block(1, 0);
        environment.block(0, 1);

        List<BlockVector> path = new PatheticPathPlanner(environment)
                .findPath(new BlockVector(0, 0, 0), new BlockVector(3, 0, 3));

        assertThat(path).isNotEmpty();
        assertThat(path).doesNotContain(new BlockVector(1, 0, 1));
    }

    @Test
    void rejectsPartialFallbackWhenGoalIsCompletelyEnclosed() {
        GridEnvironment environment = new GridEnvironment(-8, 8);
        BlockVector target = new BlockVector(4, 0, 4);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx != 0 || dz != 0) {
                    environment.block(target.getBlockX() + dx, target.getBlockZ() + dz);
                }
            }
        }

        List<BlockVector> path =
                new PatheticPathPlanner(environment).findPath(new BlockVector(0, 0, 0), target);

        assertThat(path).isEmpty();
    }

    @Test
    void exitsDeadEndBeforeHeadingTowardsTarget() {
        GridEnvironment environment = new GridEnvironment(-16, 16);

        for (int x = -1; x <= 5; x++) {
            environment.block(x, -1);
            environment.block(x, 1);
        }
        for (int z = -1; z <= 1; z++) {
            environment.block(5, z);
        }

        BlockVector start = new BlockVector(0, 0, 0);
        BlockVector target = new BlockVector(9, 0, 0);
        List<BlockVector> path = new PatheticPathPlanner(environment).findPath(start, target);

        assertThat(path).isNotEmpty();
        assertThat(path.get(0)).isEqualTo(start);
        assertThat(path.get(path.size() - 1)).isEqualTo(target);
        assertThat(path).noneMatch(environment::isBlocked);
        assertThat(path).anyMatch(position -> position.getBlockX() <= -2);
    }

    @Test
    void routesAroundTemporarilyFailedWaypoint() {
        GridEnvironment environment = new GridEnvironment(-12, 12);
        BlockVector failedWaypoint = new BlockVector(2, 0, 0);

        List<BlockVector> path = new PatheticPathPlanner(environment)
                .findPath(new BlockVector(0, 0, 0), new BlockVector(5, 0, 0), java.util.Collections.singleton(failedWaypoint));

        assertThat(path).isNotEmpty();
        assertThat(path).doesNotContain(failedWaypoint);
        assertThat(path.get(path.size() - 1)).isEqualTo(new BlockVector(5, 0, 0));
        assertThat(path).anyMatch(position -> position.getBlockZ() != 0);
    }

    @Test
    void choosesAlternativeGoalWhenPreferredGoalPreviouslyFailed() {
        GridEnvironment environment = new GridEnvironment(-12, 12);
        PathGoalResolver resolver = new PathGoalResolver(environment);
        BlockVector requestedGoal = new BlockVector(5, 0, 0);

        BlockVector resolved = resolver.resolve(
                new BlockVector(0, 0, 0), requestedGoal, candidate -> !candidate.equals(requestedGoal));

        assertThat(resolved).isNotNull().isNotEqualTo(requestedGoal);
        BlockVector resolvedGoal = Objects.requireNonNull(resolved, "resolved goal");
        assertThat(Math.max(Math.abs(resolvedGoal.getBlockX() - 5), Math.abs(resolvedGoal.getBlockZ())))
                .isEqualTo(1);
    }

    @Test
    void skipsOnlyConsecutivelyReachableWaypoints() {
        List<Integer> path = java.util.Arrays.asList(0, 1, 2, 3, 4, 5);

        int selected = PathWaypointSelector.furthestReachable(path, 1, 3, waypoint -> waypoint <= 3);

        assertThat(selected).isEqualTo(3);
    }

    @Test
    void doesNotSkipPastFirstUnsafeWaypoint() {
        List<Integer> path = java.util.Arrays.asList(0, 1, 2, 3, 4);

        int selected = PathWaypointSelector.furthestReachable(path, 1, 3, waypoint -> waypoint != 2);

        assertThat(selected).isEqualTo(1);
    }

    @Test
    void supportsOneBlockStepsAndControlledDrops() {
        HeightMapEnvironment environment = new HeightMapEnvironment();
        environment.setHeight(0, 0, 0);
        environment.setHeight(1, 0, 1);
        environment.setHeight(2, 0, 1);
        environment.setHeight(3, 0, -2);

        assertThat(environment.canTraverse(new BlockVector(0, 0, 0), new BlockVector(1, 1, 0)))
                .isTrue();
        assertThat(environment.canTraverse(new BlockVector(2, 1, 0), new BlockVector(3, -2, 0)))
                .isTrue();
        assertThat(environment.canTraverse(new BlockVector(0, 0, 0), new BlockVector(1, 2, 0)))
                .isFalse();
        assertThat(environment.canTraverse(new BlockVector(2, 1, 0), new BlockVector(3, -3, 0)))
                .isFalse();
    }

    @Test
    void projectsAirborneTargetOntoWalkableGround() {
        GridEnvironment environment = new GridEnvironment(-16, 16);
        PathGoalResolver resolver = new PathGoalResolver(environment);

        BlockVector goal = resolver.resolve(new BlockVector(0, 0, 0), new BlockVector(8, 15, 0));

        assertThat(goal).isEqualTo(new BlockVector(8, 0, 0));
    }

    @Test
    void projectsAirborneTargetBesideBlockedTreeColumn() {
        GridEnvironment environment = new GridEnvironment(-16, 16);
        environment.block(8, 0);
        PathGoalResolver resolver = new PathGoalResolver(environment);

        BlockVector goal = resolver.resolve(new BlockVector(0, 0, 0), new BlockVector(8, 15, 0));

        assertThat(goal).isNotNull();
        BlockVector resolvedGoal = Objects.requireNonNull(goal, "projected goal");
        assertThat(resolvedGoal.getBlockY()).isZero();
        assertThat(environment.isBlocked(resolvedGoal)).isFalse();
        assertThat(Math.max(Math.abs(resolvedGoal.getBlockX() - 8), Math.abs(resolvedGoal.getBlockZ())))
                .isEqualTo(1);
    }

    private static long pack(int x, int z) {
        return (((long) x) << 32) ^ (z & 0xffff_ffffL);
    }

    private static final class GridEnvironment implements BotTraversalEnvironment {
        private final int minimum;
        private final int maximum;
        private final Set<Long> blocked = new HashSet<>();

        private GridEnvironment(int minimum, int maximum) {
            this.minimum = minimum;
            this.maximum = maximum;
        }

        private void block(int x, int z) {
            blocked.add(pack(x, z));
        }

        private boolean isBlocked(BlockVector position) {
            return blocked.contains(pack(position.getBlockX(), position.getBlockZ()));
        }

        @Override
        public boolean canStandAt(BlockVector position) {
            return position.getBlockY() == 0
                    && position.getBlockX() >= minimum
                    && position.getBlockX() <= maximum
                    && position.getBlockZ() >= minimum
                    && position.getBlockZ() <= maximum
                    && !isBlocked(position);
        }

        @Override
        public boolean canOccupy(BlockVector position) {
            return position.getBlockX() >= minimum
                    && position.getBlockX() <= maximum
                    && position.getBlockZ() >= minimum
                    && position.getBlockZ() <= maximum
                    && !isBlocked(position);
        }
    }

    private static final class HeightMapEnvironment implements BotTraversalEnvironment {
        private final java.util.Map<Long, Integer> heights = new java.util.HashMap<>();

        private void setHeight(int x, int z, int feetY) {
            heights.put(pack(x, z), feetY);
        }

        @Override
        public boolean canStandAt(BlockVector position) {
            return heights.getOrDefault(pack(position.getBlockX(), position.getBlockZ()), Integer.MIN_VALUE)
                    == position.getBlockY();
        }

        @Override
        public boolean canOccupy(BlockVector position) {
            Integer feetY = heights.get(pack(position.getBlockX(), position.getBlockZ()));
            return feetY != null && position.getBlockY() >= feetY;
        }
    }
}
