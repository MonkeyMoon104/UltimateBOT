package com.monkey.ultimatebot.compat;

import org.bukkit.GameMode;
import org.jspecify.annotations.Nullable;

/**
 * {@code GameMode#isInvulnerable()} is Paper-only (2024). Spigot 1.8 has the enum constants but not
 * the method — a hard-linked call throws {@link NoSuchMethodError}. Equivalent check is creative or
 * spectator, both present since 1.8.
 */
public final class GameModeAccess {

    private GameModeAccess() {}

    public static boolean isInvulnerable(@Nullable GameMode gameMode) {
        return gameMode == GameMode.CREATIVE || gameMode == GameMode.SPECTATOR;
    }
}
