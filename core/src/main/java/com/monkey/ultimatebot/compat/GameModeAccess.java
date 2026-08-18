package com.monkey.ultimatebot.compat;

import org.bukkit.GameMode;
import org.jspecify.annotations.Nullable;

/**
 * {@code GameMode#isInvulnerable()} is Paper-only (2024). Spigot 1.8 has the enum constants but not
 * the method — a hard-linked call throws {@link NoSuchMethodError}. Equivalent check is creative or
 * spectator. Spectator itself is 1.8+; compare by {@code name()} so 1.7.10 never links
 * {@code GameMode.SPECTATOR}.
 */
public final class GameModeAccess {

    private GameModeAccess() {}

    public static boolean isInvulnerable(@Nullable GameMode gameMode) {
        if (gameMode == null) {
            return false;
        }
        // SPECTATOR is 1.8+; GameMode.SPECTATOR getstatic is NoSuchFieldError on 1.7.10.
        return gameMode == GameMode.CREATIVE || "SPECTATOR".equals(gameMode.name());
    }
}
