package com.monkey.ultimatebot.compat;

import org.bukkit.entity.Entity;

/**
 * Selects modern ({@link Entity#getX()}) or legacy ({@link Entity#getLocation()}) coordinate
 * ops. Modern is loaded only after probing so older Paper never links those methods.
 */
final class EntityCoordsOpsLookup {
    private static final EntityCoordsOps INSTANCE = resolve();

    private EntityCoordsOpsLookup() {}

    static EntityCoordsOps get() {
        return INSTANCE;
    }

    private static EntityCoordsOps resolve() {
        try {
            java.lang.reflect.Method probe = Entity.class.getMethod("getX");
            if (probe.getReturnType() != double.class) {
                return new LegacyEntityCoordsOps();
            }
            return Class.forName("com.monkey.ultimatebot.compat.ModernEntityCoordsOps")
                    .asSubclass(EntityCoordsOps.class)
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return new LegacyEntityCoordsOps();
        }
    }
}