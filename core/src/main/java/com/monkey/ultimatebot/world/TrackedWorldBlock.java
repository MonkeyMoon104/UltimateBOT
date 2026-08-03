package com.monkey.ultimatebot.world;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.jspecify.annotations.Nullable;

record TrackedWorldBlock(
        BlockData originalData, Material material, @Nullable ScheduledTask expiryTask) {
    void cancelExpiry() {
        if (expiryTask != null) {
            expiryTask.cancel();
        }
    }
}
