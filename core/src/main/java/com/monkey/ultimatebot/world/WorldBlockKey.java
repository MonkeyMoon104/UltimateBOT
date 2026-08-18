package com.monkey.ultimatebot.world;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jspecify.annotations.Nullable;

final class WorldBlockKey {
    private final UUID worldUUID;
    private final int x;
    private final int y;
    private final int z;

    WorldBlockKey(UUID worldUUID, int x, int y, int z) {
        this.worldUUID = worldUUID;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public UUID worldUUID() {
        return worldUUID;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int z() {
        return z;
    }

    static WorldBlockKey from(Block block) {
        return new WorldBlockKey(block.getWorld().getUID(), block.getX(), block.getY(), block.getZ());
    }

    @Nullable Block block() {
        World world = Bukkit.getWorld(worldUUID);
        return world == null ? null : world.getBlockAt(x, y, z);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof WorldBlockKey)) {
            return false;
        }
        WorldBlockKey other = (WorldBlockKey) obj;
        return java.util.Objects.equals(worldUUID, other.worldUUID) && x == other.x && y == other.y && z == other.z;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(worldUUID, x, y, z);
    }

    @Override
    public String toString() {
        return "WorldBlockKey[worldUUID=" + worldUUID + ", x=" + x + ", y=" + y + ", z=" + z + "]";
    }
}
