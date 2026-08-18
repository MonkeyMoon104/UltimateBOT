package com.monkey.ultimatebot.access.entity;

import org.bukkit.entity.Entity;

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
            return Class.forName("com.monkey.ultimatebot.access.entity.ModernEntityCoordsOps")
                    .asSubclass(EntityCoordsOps.class)
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return new LegacyEntityCoordsOps();
        }
    }
}
