package com.monkey.ultimatebot.bot.ai.controllers.enderpearl.helper;

import com.monkey.ultimatebot.bot.ai.controllers.enderpearl.helper.inter.ISafetyValidator;
import com.monkey.ultimatebot.access.block.BlockPassableAccess;
import com.monkey.ultimatebot.access.item.MaterialAirAccess;
import com.monkey.ultimatebot.utils.material.MaterialCatalog;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

public class SafetyValidator implements ISafetyValidator {

    private final World world;

    public SafetyValidator(World world) {
        this.world = world;
    }

    @Override
    public boolean isSafeLandingSpot(BlockVector pos) {
        Block stateAt = blockAt(pos);
        Block stateAbove = stateAt.getRelative(0, 1, 0);
        Block stateBelow = stateAt.getRelative(0, -1, 0);

        if (!isPassable(stateAt) || !isPassable(stateAbove)) {
            return false;
        }

        if (!isSolid(stateBelow)) {
            return false;
        }

        Material belowType = stateBelow.getType();
        return belowType != Material.CACTUS && !MaterialCatalog.is(belowType, "MAGMA_BLOCK") && !isFluid(belowType);
    }

    @Override
    public @Nullable Vector findSafeLandingSpot(BlockVector center) {
        for (int radius = 1; radius <= 3; radius++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    for (int y = -1; y <= 2; y++) {
                        BlockVector checkPos =
                                new BlockVector(center.getBlockX() + x, center.getBlockY() + y, center.getBlockZ() + z);
                        if (isSafeLandingSpot(checkPos)) {
                            return centerOf(checkPos);
                        }
                    }
                }
            }
        }
        return null;
    }

    private Block blockAt(BlockVector value) {
        return world.getBlockAt(value.getBlockX(), value.getBlockY(), value.getBlockZ());
    }

    private static Vector centerOf(BlockVector value) {
        return new Vector(value.getBlockX() + 0.5D, value.getBlockY() + 0.5D, value.getBlockZ() + 0.5D);
    }

    private static boolean isPassable(Block block) {
        Material type = block.getType();
        return MaterialAirAccess.isAir(type) || BlockPassableAccess.isPassable(block);
    }

    private static boolean isSolid(Block block) {
        Material type = block.getType();
        return type.isBlock() && type.isSolid() && !BlockPassableAccess.isPassable(block);
    }

    private static boolean isFluid(Material type) {
        return type == Material.LAVA
                || type == Material.WATER
                || "STATIONARY_LAVA".equals(type.name())
                || "STATIONARY_WATER".equals(type.name());
    }
}
