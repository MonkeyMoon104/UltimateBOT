package com.monkey.ultimatebot.bot.ai.controllers.movement.pathfinding;

import org.bukkit.util.BlockVector;

public interface BotTraversalEnvironment {
    int MAX_STEP_UP = 1;
    int MAX_SAFE_DROP = 3;

    boolean canStandAt(BlockVector position);

    boolean canOccupy(BlockVector position);

    default boolean canTraverse(BlockVector from, BlockVector to) {
        int dx = to.getBlockX() - from.getBlockX();
        int dy = to.getBlockY() - from.getBlockY();
        int dz = to.getBlockZ() - from.getBlockZ();

        if ((dx == 0 && dz == 0)
                || Math.abs(dx) > 1
                || Math.abs(dz) > 1
                || dy > MAX_STEP_UP
                || dy < -MAX_SAFE_DROP
                || !canStandAt(to)) {
            return false;
        }

        if (dy < 0) {
            for (int y = to.getBlockY() + 1; y <= from.getBlockY(); y++) {
                if (!canOccupy(new BlockVector(to.getBlockX(), y, to.getBlockZ()))) {
                    return false;
                }
            }
        }

        if (dx != 0 && dz != 0) {
            BlockVector xSide = new BlockVector(from.getBlockX() + dx, to.getBlockY(), from.getBlockZ());
            BlockVector zSide = new BlockVector(from.getBlockX(), to.getBlockY(), from.getBlockZ() + dz);
            if (!canOccupy(xSide) || !canOccupy(zSide)) {
                return false;
            }
        }

        return true;
    }

    default double additionalTraversalCost(BlockVector from, BlockVector to) {
        int dy = to.getBlockY() - from.getBlockY();
        int dx = Math.abs(to.getBlockX() - from.getBlockX());
        int dz = Math.abs(to.getBlockZ() - from.getBlockZ());

        double cost = dx != 0 && dz != 0 ? 0.05D : 0.0D;
        if (dy > 0) {
            cost += 0.8D;
        } else if (dy < 0) {
            cost += Math.abs(dy) * 0.25D;
        }
        return cost;
    }
}
