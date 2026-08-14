package com.monkey.ultimatebot.compat;

import org.bukkit.entity.Player;

interface UpdateNotifyOps {
    void sendUpdateAvailable(Player player, String currentVersion, String latestVersion, String downloadUrl);
}
