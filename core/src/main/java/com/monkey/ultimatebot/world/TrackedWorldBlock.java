package com.monkey.ultimatebot.world;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.jspecify.annotations.Nullable;

final class TrackedWorldBlock {
    private final BlockData originalData;
    private final Material material;
    private final @Nullable ScheduledTask expiryTask;

    TrackedWorldBlock(BlockData originalData, Material material, @Nullable ScheduledTask expiryTask) {
        this.originalData = originalData;
        this.material = material;
        this.expiryTask = expiryTask;
    }

    public BlockData originalData() {
        return originalData;
    }
    public Material material() {
        return material;
    }
    public @Nullable ScheduledTask expiryTask() {
        return expiryTask;
    }

    void cancelExpiry() {
        if (expiryTask != null) {
            expiryTask.cancel();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TrackedWorldBlock)) {
            return false;
        }
        TrackedWorldBlock other = (TrackedWorldBlock) obj;
        return java.util.Objects.equals(originalData, other.originalData) && java.util.Objects.equals(material, other.material) && java.util.Objects.equals(expiryTask, other.expiryTask);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(originalData, material, expiryTask);
    }

    @Override
    public String toString() {
        return "TrackedWorldBlock[originalData=" + originalData + ", material=" + material + ", expiryTask=" + expiryTask + "]";
    }
}
