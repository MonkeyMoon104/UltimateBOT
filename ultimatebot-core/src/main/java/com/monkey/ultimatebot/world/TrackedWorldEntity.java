package com.monkey.ultimatebot.world;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.jspecify.annotations.Nullable;

record TrackedWorldEntity(
        boolean explosionBlockDamageAllowed, @Nullable ScheduledTask expiryTask) {
    void cancelExpiry() {
        if (expiryTask != null) {
            expiryTask.cancel();
        }
    }
}
