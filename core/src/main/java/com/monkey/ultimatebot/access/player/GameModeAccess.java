package com.monkey.ultimatebot.access.player;

import org.bukkit.GameMode;
import org.jspecify.annotations.Nullable;

public final class GameModeAccess {

    private GameModeAccess() {}

    public static boolean isInvulnerable(@Nullable GameMode gameMode) {
        if (gameMode == null) {
            return false;
        }

        return gameMode == GameMode.CREATIVE || "SPECTATOR".equals(gameMode.name());
    }
}
