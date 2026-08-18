package com.monkey.ultimatebot.access.update;

import org.bukkit.entity.Player;

interface UpdateNotifyOps {
    void sendUpdateAvailable(Player player, String currentVersion, String latestVersion, String downloadUrl);
}
