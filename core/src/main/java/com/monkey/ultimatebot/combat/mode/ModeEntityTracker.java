package com.monkey.ultimatebot.combat.mode;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

final class ModeEntityTracker implements AutoCloseable {
    private static final int MAX_PROJECTILE_LIFETIME_TICKS = 200;

    private final Set<UUID> entityIds = new HashSet<>();

    <T extends Entity> T track(T entity) {
        entityIds.add(entity.getUniqueId());
        return entity;
    }

    void prune() {
        entityIds.removeIf(entityId -> {
            Entity entity = Bukkit.getEntity(entityId);
            if (entity == null || !entity.isValid()) {
                return true;
            }
            if (entity.getTicksLived() <= MAX_PROJECTILE_LIFETIME_TICKS) {
                return false;
            }
            entity.remove();
            return true;
        });
    }

    void remove(UUID entityId) {
        entityIds.remove(entityId);
        Entity entity = Bukkit.getEntity(entityId);
        if (entity != null) {
            entity.remove();
        }
    }

    @Override
    public void close() {
        for (UUID entityId : Set.copyOf(entityIds)) {
            remove(entityId);
        }
        entityIds.clear();
    }
}
