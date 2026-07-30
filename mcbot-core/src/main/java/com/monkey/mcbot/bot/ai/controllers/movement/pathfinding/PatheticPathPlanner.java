package com.monkey.mcbot.bot.ai.controllers.movement.pathfinding;

import de.bsommerfeld.pathetic.api.pathing.INeighborStrategy;
import de.bsommerfeld.pathetic.api.pathing.Pathfinder;
import de.bsommerfeld.pathetic.api.pathing.configuration.PathfinderConfiguration;
import de.bsommerfeld.pathetic.api.pathing.heuristic.HeuristicStrategies;
import de.bsommerfeld.pathetic.api.pathing.processing.Cost;
import de.bsommerfeld.pathetic.api.pathing.processing.ValidationProcessor;
import de.bsommerfeld.pathetic.api.pathing.result.PathfinderResult;
import de.bsommerfeld.pathetic.api.provider.NavigationPoint;
import de.bsommerfeld.pathetic.api.provider.NavigationPointProvider;
import de.bsommerfeld.pathetic.api.wrapper.PathPosition;
import de.bsommerfeld.pathetic.api.wrapper.PathVector;
import de.bsommerfeld.pathetic.engine.factory.AStarPathfinderFactory;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.minecraft.core.BlockPos;

/** Pure path planning layer. Entity movement remains the responsibility of the bot controller. */
public final class PatheticPathPlanner {
    private static final int MAX_ITERATIONS = 12_000;
    private static final int MAX_PATH_LENGTH = 192;
    private static final List<PathVector> WALKING_OFFSETS = createWalkingOffsets();

    private final BotTraversalEnvironment environment;
    private final Pathfinder pathfinder;
    private Set<Long> excludedPositions = Set.of();

    public PatheticPathPlanner(BotTraversalEnvironment environment) {
        this.environment = Objects.requireNonNull(environment, "environment");

        NavigationPointProvider provider = (position, context) -> navigationPoint(position);
        ValidationProcessor traversalValidator = context -> {
            BlockPos current = toBlockPos(context.getCurrentPathPosition());
            boolean currentTraversable = context.getNavigationPointProvider()
                    .getNavigationPoint(context.getCurrentPathPosition(), context.getEnvironmentContext())
                    .isTraversable();
            if (!currentTraversable || context.getPreviousPathPosition() == null) {
                return currentTraversable;
            }
            return environment.canTraverse(toBlockPos(context.getPreviousPathPosition()), current);
        };

        PathfinderConfiguration configuration = PathfinderConfiguration.builder()
                .maxIterations(MAX_ITERATIONS)
                .maxLength(MAX_PATH_LENGTH)
                .async(false)
                .fallback(false)
                .provider(provider)
                .validationProcessors(List.of(traversalValidator))
                .costProcessor(List.of(context -> context.getPreviousPathPosition() == null
                        ? Cost.ZERO
                        : Cost.of(environment.additionalTraversalCost(
                                toBlockPos(context.getPreviousPathPosition()),
                                toBlockPos(context.getCurrentPathPosition())))))
                .neighborStrategy(walkingNeighborStrategy())
                .heuristicStrategy(HeuristicStrategies.LINEAR)
                .reopenClosedNodes(true)
                .build();

        this.pathfinder = new AStarPathfinderFactory().createPathfinder(configuration);
    }

    public List<BlockPos> findPath(BlockPos start, BlockPos target) {
        return findPath(start, target, Set.of());
    }

    public List<BlockPos> findPath(BlockPos start, BlockPos target, Set<BlockPos> excluded) {
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(excluded, "excluded");

        Set<Long> previousExcludedPositions = excludedPositions;
        excludedPositions = new HashSet<>(excluded.size());
        for (BlockPos position : excluded) {
            excludedPositions.add(position.asLong());
        }

        PathfinderResult result;
        try {
            result = pathfinder
                    .findPath(toPathPosition(start), toPathPosition(target))
                    .resultBlocking();
        } finally {
            excludedPositions = previousExcludedPositions;
        }

        if (!result.successful() || result.getPath().length() < 2) {
            return List.of();
        }

        List<BlockPos> path = result.getPath().collect().stream()
                .map(PatheticPathPlanner::toBlockPos)
                .distinct()
                .toList();
        return path;
    }

    private NavigationPoint navigationPoint(PathPosition position) {
        BlockPos blockPosition = toBlockPos(position);
        boolean traversable =
                !excludedPositions.contains(blockPosition.asLong()) && environment.canStandAt(blockPosition);
        return () -> traversable;
    }

    private static INeighborStrategy walkingNeighborStrategy() {
        return () -> WALKING_OFFSETS;
    }

    private static List<PathVector> createWalkingOffsets() {
        List<PathVector> offsets = new ArrayList<>(40);
        for (int dy = BotTraversalEnvironment.MAX_STEP_UP; dy >= -BotTraversalEnvironment.MAX_SAFE_DROP; dy--) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx != 0 || dz != 0) {
                        offsets.add(PathVector.of(dx, dy, dz));
                    }
                }
            }
        }
        return List.copyOf(offsets);
    }

    private static PathPosition toPathPosition(BlockPos position) {
        return PathPosition.of(position.getX(), position.getY(), position.getZ());
    }

    private static BlockPos toBlockPos(PathPosition position) {
        return new BlockPos(position.getFlooredX(), position.getFlooredY(), position.getFlooredZ());
    }
}
