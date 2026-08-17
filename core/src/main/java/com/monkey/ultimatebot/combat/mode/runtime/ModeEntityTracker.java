package com.monkey.ultimatebot.combat.mode.runtime;


import com.monkey.ultimatebot.compat.EntityLookupAccess;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.entity.Entity;

public final class ModeEntityTracker implements AutoCloseable {
    private static final int MAX_PROJECTILE_LIFETIME_TICKS = 200;

    private final Set<UUID> entityIds = new HashSet<>();

    public <T extends Entity> T track(T entity) {
        entityIds.add(entity.getUniqueId());
        return entity;
    }

    public void prune() {
        entityIds.removeIf(entityId -> {
            Entity entity = EntityLookupAccess.get(entityId);
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

    public void remove(UUID entityId) {
        entityIds.remove(entityId);
        Entity entity = EntityLookupAccess.get(entityId);
        if (entity != null) {
            entity.remove();
        }
    }

    @Override
    public void close() {
        for (UUID entityId : com.monkey.ultimatebot.common.util.ImmutableCollections.copyOf(entityIds)) {
            remove(entityId);
        }
        entityIds.clear();
    }
}
