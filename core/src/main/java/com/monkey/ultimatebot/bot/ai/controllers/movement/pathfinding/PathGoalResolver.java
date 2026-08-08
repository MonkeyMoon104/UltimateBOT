package com.monkey.ultimatebot.bot.ai.controllers.movement.pathfinding;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import org.bukkit.util.BlockVector;
import org.jspecify.annotations.Nullable;

/** Projects airborne or obstructed targets onto nearby terrain that a walking bot can reach. */
final class PathGoalResolver {
    private static final int HORIZONTAL_SEARCH_RADIUS = 6;
    private static final int VERTICAL_SEARCH_RADIUS = 12;
    private static final List<GoalOffset> SEARCH_OFFSETS = createSearchOffsets();

    private final BotTraversalEnvironment environment;

    PathGoalResolver(BotTraversalEnvironment environment) {
        this.environment = Objects.requireNonNull(environment, "environment");
    }

    @Nullable BlockVector resolve(BlockVector start, BlockVector requestedGoal) {
        return resolve(start, requestedGoal, ignored -> true);
    }

    @Nullable BlockVector resolve(BlockVector start, BlockVector requestedGoal, Predicate<BlockVector> candidateFilter) {
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(requestedGoal, "requestedGoal");
        Objects.requireNonNull(candidateFilter, "candidateFilter");

        if (environment.canStandAt(requestedGoal)
                && candidateFilter.test(requestedGoal)
                && Math.abs(requestedGoal.getBlockY() - start.getBlockY()) <= VERTICAL_SEARCH_RADIUS) {
            return requestedGoal;
        }

        for (GoalOffset offset : SEARCH_OFFSETS) {
            BlockVector candidate = new BlockVector(
                    requestedGoal.getBlockX() + offset.x(),
                    start.getBlockY() + offset.y(),
                    requestedGoal.getBlockZ() + offset.z());
            if (environment.canStandAt(candidate) && candidateFilter.test(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private static List<GoalOffset> createSearchOffsets() {
        List<GoalOffset> offsets = new ArrayList<>();
        for (int x = -HORIZONTAL_SEARCH_RADIUS; x <= HORIZONTAL_SEARCH_RADIUS; x++) {
            for (int z = -HORIZONTAL_SEARCH_RADIUS; z <= HORIZONTAL_SEARCH_RADIUS; z++) {
                for (int y = -VERTICAL_SEARCH_RADIUS; y <= VERTICAL_SEARCH_RADIUS; y++) {
                    offsets.add(new GoalOffset(x, y, z));
                }
            }
        }
        offsets.sort(Comparator.comparingDouble(GoalOffset::cost).thenComparingInt(offset -> Math.abs(offset.y())));
        return List.copyOf(offsets);
    }

    private record GoalOffset(int x, int y, int z) {
        private double cost() {
            return (double) x * x + (double) z * z + Math.abs(y) * 1.75D;
        }
    }
}
