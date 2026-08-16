package com.monkey.ultimatebot.world;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.Objects;
import org.bukkit.Material;
import org.jspecify.annotations.Nullable;

final class TrackedWorldBlock {
    private final Object originalSnapshot;
    private final Material material;
    private final @Nullable ScheduledTask expiryTask;

    TrackedWorldBlock(Object originalSnapshot, Material material, @Nullable ScheduledTask expiryTask) {
        this.originalSnapshot = Objects.requireNonNull(originalSnapshot, "originalSnapshot");
        this.material = Objects.requireNonNull(material, "material");
        this.expiryTask = expiryTask;
    }

    public Object originalSnapshot() {
        return originalSnapshot;
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
        return Objects.equals(originalSnapshot, other.originalSnapshot)
                && Objects.equals(material, other.material)
                && Objects.equals(expiryTask, other.expiryTask);
    }

    @Override
    public int hashCode() {
        return Objects.hash(originalSnapshot, material, expiryTask);
    }

    @Override
    public String toString() {
        return "TrackedWorldBlock[originalSnapshot="
                + originalSnapshot
                + ", material="
                + material
                + ", expiryTask="
                + expiryTask
                + "]";
    }
}
