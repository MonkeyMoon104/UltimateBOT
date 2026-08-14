package com.monkey.ultimatebot.compat;

import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

/** Paper with Adventure on the classpath (1.16.5+). */
final class ModernUpdateNotifyOps implements UpdateNotifyOps {
    @Override
    public void sendUpdateAvailable(Player player, String currentVersion, String latestVersion, String downloadUrl) {
        Objects.requireNonNull(player, "player");
        player.sendMessage(Component.text("[UltimateBot] ", NamedTextColor.GOLD)
                .append(Component.text("New version available: ", NamedTextColor.YELLOW))
                .append(Component.text(latestVersion, NamedTextColor.GREEN))
                .append(Component.text(" (actually " + currentVersion + ") ", NamedTextColor.GRAY))
                .append(Component.text("[Click to open link]", NamedTextColor.AQUA)
                        .clickEvent(ClickEvent.openUrl(downloadUrl))
                        .hoverEvent(HoverEvent.showText(Component.text("Open download page", NamedTextColor.GREEN)))));
    }
}
