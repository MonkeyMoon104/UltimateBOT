package com.monkey.ultimatebot.compat;

import org.bukkit.entity.minecart.ExplosiveMinecart;

/**
 * Selects modern ({@link ExplosiveMinecart#setFuseTicks(int)}) or legacy (Craft/NMS reflection)
 * fuse ops. Modern is loaded only after probing so older Paper never links the interface method.
 */
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
            return Class.forName("com.monkey.ultimatebot.compat.ModernExplosiveMinecartOps")
                    .asSubclass(ExplosiveMinecartOps.class)
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return new LegacyExplosiveMinecartOps();
        }
    }
}
