package com.monkey.ultimatebot.access.explosive;

import org.bukkit.entity.minecart.ExplosiveMinecart;

final class ExplosiveMinecartOpsLookup {
    private static final ExplosiveMinecartOps INSTANCE = resolve();

    private ExplosiveMinecartOpsLookup() {}

    static ExplosiveMinecartOps get() {
        return INSTANCE;
    }

    private static ExplosiveMinecartOps resolve() {
        try {
            java.lang.reflect.Method probe = ExplosiveMinecart.class.getMethod("setFuseTicks", int.class);
            if (probe.getReturnType() != void.class) {
                return new LegacyExplosiveMinecartOps();
            }
            return Class.forName("com.monkey.ultimatebot.access.explosive.ModernExplosiveMinecartOps")
                    .asSubclass(ExplosiveMinecartOps.class)
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return new LegacyExplosiveMinecartOps();
        }
    }
}
