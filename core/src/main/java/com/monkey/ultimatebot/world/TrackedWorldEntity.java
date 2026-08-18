package com.monkey.ultimatebot.world;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.jspecify.annotations.Nullable;

final class TrackedWorldEntity {
    private final boolean explosionBlockDamageAllowed;
    private final @Nullable ScheduledTask expiryTask;

    TrackedWorldEntity(boolean explosionBlockDamageAllowed, @Nullable ScheduledTask expiryTask) {
        this.explosionBlockDamageAllowed = explosionBlockDamageAllowed;
        this.expiryTask = expiryTask;
    }

    public boolean explosionBlockDamageAllowed() {
        return explosionBlockDamageAllowed;
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
        if (!(obj instanceof TrackedWorldEntity)) {
            return false;
        }
        TrackedWorldEntity other = (TrackedWorldEntity) obj;
        return explosionBlockDamageAllowed == other.explosionBlockDamageAllowed
                && java.util.Objects.equals(expiryTask, other.expiryTask);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(explosionBlockDamageAllowed, expiryTask);
    }

    @Override
    public String toString() {
        return "TrackedWorldEntity[explosionBlockDamageAllowed=" + explosionBlockDamageAllowed + ", expiryTask="
                + expiryTask + "]";
    }
}
