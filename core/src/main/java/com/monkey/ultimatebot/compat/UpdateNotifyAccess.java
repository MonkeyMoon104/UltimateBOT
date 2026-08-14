package com.monkey.ultimatebot.compat;

import java.util.Objects;
import org.bukkit.entity.Player;

/**
 * Admin update-available chat across Paper versions.
 *
 * <p>Paper 1.16.5+ has Adventure ({@code Player#sendMessage(Component)}). Paper 1.16.4 shares Craft
 * {@code v1_16_R3} but has no Adventure on the classpath. Dual-path: never hard-link Adventure from
 * call sites.
 */
public final class UpdateNotifyAccess {

    private UpdateNotifyAccess() {}

    public static void sendUpdateAvailable(
            Player player, String currentVersion, String latestVersion, String downloadUrl) {
        UpdateNotifyOpsLookup.get()
                .sendUpdateAvailable(
                        Objects.requireNonNull(player, "player"),
                        Objects.requireNonNull(currentVersion, "currentVersion"),
                        Objects.requireNonNull(latestVersion, "latestVersion"),
                        Objects.requireNonNull(downloadUrl, "downloadUrl"));
    }
}
