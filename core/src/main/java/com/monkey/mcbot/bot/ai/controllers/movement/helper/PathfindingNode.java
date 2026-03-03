package com.monkey.mcbot.bot.ai.controllers.movement.helper;

import net.minecraft.core.BlockPos;

public class PathfindingNode implements Comparable<PathfindingNode> {
    public BlockPos position;
    public PathfindingNode cameFrom;
    public double gScore;
    public double fScore;

    public PathfindingNode(BlockPos position, PathfindingNode cameFrom, double gScore, double hScore) {
        this.position = position;
        this.cameFrom = cameFrom;
        this.gScore = gScore;
        this.fScore = gScore + hScore;
    }

    @Override
    public int compareTo(PathfindingNode other) {
        return Double.compare(this.fScore, other.fScore);
    }
}