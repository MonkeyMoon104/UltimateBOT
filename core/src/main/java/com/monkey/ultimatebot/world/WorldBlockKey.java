package com.monkey.ultimatebot.world;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jspecify.annotations.Nullable;

record WorldBlockKey(UUID worldUUID, int x, int y, int z) {
    static WorldBlockKey from(Block block) {
        return new WorldBlockKey(block.getWorld().getUID(), block.getX(), block.getY(), block.getZ());
    }

    @Nullable Block block() {
        World world = Bukkit.getWorld(worldUUID);
        return world == null ? null : world.getBlockAt(x, y, z);
    }
}
