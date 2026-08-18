package com.monkey.ultimatebot.access.update;

import java.util.Objects;
import org.bukkit.entity.Player;

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
