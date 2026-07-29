package com.monkey.mcbot.bot.ai.controllers.combat;

import java.util.function.Supplier;

/**
 * Marks the synchronous Bukkit explosion event fired by a bot combat action.
 */
public final class BotExplosionContext {
    private static final ThreadLocal<Integer> BLOCK_PROTECTION_DEPTH = ThreadLocal.withInitial(() -> 0);

    private BotExplosionContext() {
    }

    public static <T> T execute(boolean blockDamage, Supplier<T> action) {
        if (blockDamage) {
            return action.get();
        }

        BLOCK_PROTECTION_DEPTH.set(BLOCK_PROTECTION_DEPTH.get() + 1);
        try {
            return action.get();
        } finally {
            int remaining = BLOCK_PROTECTION_DEPTH.get() - 1;
            if (remaining <= 0) {
                BLOCK_PROTECTION_DEPTH.remove();
            } else {
                BLOCK_PROTECTION_DEPTH.set(remaining);
            }
        }
    }

    public static boolean isBlockDamageSuppressed() {
        return BLOCK_PROTECTION_DEPTH.get() > 0;
    }
}
