package com.monkey.ultimatebot.combat.mode;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.jspecify.annotations.Nullable;

final class ModeBlockTracker implements AutoCloseable {
    private final Set<BlockKey> blocks = new HashSet<>();

    boolean place(Location location, Material material) {
        Objects.requireNonNull(location, "location");
        Material checkedMaterial = Objects.requireNonNull(material, "material");
        Block block = location.getBlock();
        if (!block.isPassable()
                || block.isLiquid()
                || !block.getRelative(0, -1, 0).getType().isSolid()) {
            return false;
        }
        blocks.add(BlockKey.from(block));
        broadcast(block.getLocation(), checkedMaterial.createBlockData());
        return true;
    }

    void restore(Location location) {
        Objects.requireNonNull(location, "location");
        Block block = location.getBlock();
        if (blocks.remove(BlockKey.from(block))) {
            broadcast(block.getLocation(), block.getBlockData());
        }
    }

    @Override
    public void close() {
        for (BlockKey key : Set.copyOf(blocks)) {
            Block block = key.block();
            if (block != null) {
                broadcast(block.getLocation(), block.getBlockData());
            }
        }
        blocks.clear();
    }

    private static void broadcast(Location location, BlockData blockData) {
        World world = Objects.requireNonNull(location.getWorld(), "location world");
        for (org.bukkit.entity.Player viewer : world.getPlayers()) {
            viewer.sendBlockChange(location, blockData);
        }
    }

    private record BlockKey(UUID worldId, int x, int y, int z) {
        static BlockKey from(Block block) {
            return new BlockKey(block.getWorld().getUID(), block.getX(), block.getY(), block.getZ());
        }

        @Nullable Block block() {
            World world = org.bukkit.Bukkit.getWorld(worldId);
            return world == null ? null : world.getBlockAt(x, y, z);
        }
    }
}
