package com.monkey.ultimatebot.combat.mode.runtime;


import java.util.Collections;
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

public final class ModeBlockTracker implements AutoCloseable {
    private final Set<BlockKey> blocks = new HashSet<>();

    public boolean canPlace(Location location, Material material) {
        Objects.requireNonNull(location, "location");
        Material checkedMaterial = Objects.requireNonNull(material, "material");
        Block block = location.getBlock();
        return block.isPassable()
                && !block.isLiquid()
                && (!requiresSupport(checkedMaterial)
                        || block.getRelative(0, -1, 0).getType().isSolid());
    }

    public boolean place(Location location, Material material) {
        Objects.requireNonNull(location, "location");
        Material checkedMaterial = Objects.requireNonNull(material, "material");
        Block block = location.getBlock();
        if (!canPlace(location, checkedMaterial)) {
            return false;
        }
        blocks.add(BlockKey.from(block));
        broadcast(block.getLocation(), checkedMaterial.createBlockData());
        return true;
    }

    public void restore(Location location) {
        Objects.requireNonNull(location, "location");
        Block block = location.getBlock();
        if (blocks.remove(BlockKey.from(block))) {
            broadcast(block.getLocation(), block.getBlockData());
        }
    }

    @Override
    public void close() {
        for (BlockKey key : com.monkey.ultimatebot.common.util.ImmutableCollections.copyOf(blocks)) {
            Block block = key.block();
            if (block != null) {
                broadcast(block.getLocation(), block.getBlockData());
            }
        }
        blocks.clear();
    }

    private static boolean requiresSupport(Material material) {
        return material == Material.RAIL;
    }

    private static void broadcast(Location location, BlockData blockData) {
        World world = Objects.requireNonNull(location.getWorld(), "location world");
        for (org.bukkit.entity.Player viewer : world.getPlayers()) {
            viewer.sendBlockChange(location, blockData);
        }
    }

    private static final class BlockKey {
        private final UUID worldId;
        private final int x;
        private final int y;
        private final int z;

        private BlockKey(UUID worldId, int x, int y, int z) {
            this.worldId = worldId;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        static BlockKey from(Block block) {
            return new BlockKey(block.getWorld().getUID(), block.getX(), block.getY(), block.getZ());
        }

        @Nullable Block block() {
            World world = org.bukkit.Bukkit.getWorld(worldId);
            return world == null ? null : world.getBlockAt(x, y, z);
        }
    

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof BlockKey)) {
                return false;
            }
            BlockKey other = (BlockKey) obj;
            return java.util.Objects.equals(worldId, other.worldId) && x == other.x && y == other.y && z == other.z;
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(worldId, x, y, z);
        }

        @Override
        public String toString() {
            return "BlockKey[worldId=" + worldId + ", x=" + x + ", y=" + y + ", z=" + z + "]";
        }
    }
}
