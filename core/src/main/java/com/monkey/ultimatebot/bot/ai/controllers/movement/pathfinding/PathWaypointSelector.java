package com.monkey.ultimatebot.bot.ai.controllers.movement.pathfinding;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

final class PathWaypointSelector {
    private PathWaypointSelector() {}

    static <T> int furthestReachable(
            List<T> path, int currentIndex, int maximumLookahead, Predicate<T> segmentIsClear) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(segmentIsClear, "segmentIsClear");
        if (currentIndex < 0 || currentIndex >= path.size() || maximumLookahead <= 0) {
            return currentIndex;
        }

        int furthest = currentIndex;
        int limit = Math.min(path.size() - 1, currentIndex + maximumLookahead);
        for (int index = currentIndex + 1; index <= limit; index++) {
            if (!segmentIsClear.test(path.get(index))) {
                break;
            }
            furthest = index;
        }
        return furthest;
    }
}
