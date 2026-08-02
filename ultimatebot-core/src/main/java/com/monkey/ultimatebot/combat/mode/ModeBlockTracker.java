package com.monkey.ultimatebot.combat.mode;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.jspecify.annotations.Nullable;

final class ModeBlockTracker implements AutoCloseable {
    private final Map<BlockKey, TrackedBlock> blocks = new HashMap<>();

    boolean place(Location location, Material material) {
        Objects.requireNonNull(location, "location");
        Material checkedMaterial = Objects.requireNonNull(material, "material");
        Block block = location.getBlock();
        if (!block.isPassable() || block.isLiquid()) {
            return false;
        }
        BlockKey key = BlockKey.from(block);
        blocks.computeIfAbsent(key, ignored -> new TrackedBlock(block.getState(), checkedMaterial));
        block.setType(checkedMaterial, false);
        return true;
    }

    void restore(Location location) {
        Objects.requireNonNull(location, "location");
        Block block = location.getBlock();
        BlockKey key = BlockKey.from(block);
        TrackedBlock tracked = blocks.remove(key);
        if (tracked != null) {
            tracked.restore(block);
        }
    }

    @Override
    public void close() {
        for (Map.Entry<BlockKey, TrackedBlock> entry : Map.copyOf(blocks).entrySet()) {
            Block block = entry.getKey().block();
            if (block != null) {
                entry.getValue().restore(block);
            }
        }
        blocks.clear();
    }

    private record TrackedBlock(BlockState original, Material placedMaterial) {
        private TrackedBlock {
            Objects.requireNonNull(original, "original");
            Objects.requireNonNull(placedMaterial, "placedMaterial");
        }

        void restore(Block current) {
            if (current.getType() == placedMaterial) {
                original.update(true, false);
            }
        }
    }

    private record BlockKey(UUID worldId, int x, int y, int z) {
        static BlockKey from(Block block) {
            return new BlockKey(block.getWorld().getUID(), block.getX(), block.getY(), block.getZ());
        }

        @Nullable Block block() {
            World world = org.bukkit.Bukkit.getWorld(worldId);
            if (world == null) {
                return null;
            }
            return world.getBlockAt(x, y, z);
        }
    }
}
