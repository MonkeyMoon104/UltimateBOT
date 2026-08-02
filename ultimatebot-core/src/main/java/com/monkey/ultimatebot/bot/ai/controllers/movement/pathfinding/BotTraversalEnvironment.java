package com.monkey.ultimatebot.bot.ai.controllers.movement.pathfinding;

import net.minecraft.core.BlockPos;

/** Defines the movements a player-sized training bot can physically perform. */
public interface BotTraversalEnvironment {
    int MAX_STEP_UP = 1;
    int MAX_SAFE_DROP = 3;

    boolean canStandAt(BlockPos position);

    boolean canOccupy(BlockPos position);

    default boolean canTraverse(BlockPos from, BlockPos to) {
        int dx = to.getX() - from.getX();
        int dy = to.getY() - from.getY();
        int dz = to.getZ() - from.getZ();

        if ((dx == 0 && dz == 0)
                || Math.abs(dx) > 1
                || Math.abs(dz) > 1
                || dy > MAX_STEP_UP
                || dy < -MAX_SAFE_DROP
                || !canStandAt(to)) {
            return false;
        }

        if (dy < 0) {
            for (int y = to.getY() + 1; y <= from.getY(); y++) {
                if (!canOccupy(new BlockPos(to.getX(), y, to.getZ()))) {
                    return false;
                }
            }
        }

        if (dx != 0 && dz != 0) {
            BlockPos xSide = new BlockPos(from.getX() + dx, to.getY(), from.getZ());
            BlockPos zSide = new BlockPos(from.getX(), to.getY(), from.getZ() + dz);
            if (!canOccupy(xSide) || !canOccupy(zSide)) {
                return false;
            }
        }

        return true;
    }

    default double additionalTraversalCost(BlockPos from, BlockPos to) {
        int dy = to.getY() - from.getY();
        int dx = Math.abs(to.getX() - from.getX());
        int dz = Math.abs(to.getZ() - from.getZ());

        double cost = dx != 0 && dz != 0 ? 0.05D : 0.0D;
        if (dy > 0) {
            cost += 0.8D;
        } else if (dy < 0) {
            cost += Math.abs(dy) * 0.25D;
        }
        return cost;
    }
}
