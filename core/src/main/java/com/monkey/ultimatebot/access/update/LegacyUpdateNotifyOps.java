package com.monkey.ultimatebot.access.update;

import java.util.Objects;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

final class LegacyUpdateNotifyOps implements UpdateNotifyOps {
    @Override
    public void sendUpdateAvailable(Player player, String currentVersion, String latestVersion, String downloadUrl) {
        Objects.requireNonNull(player, "player");
        TextComponent root = new TextComponent("[UltimateBot] ");
        root.setColor(ChatColor.GOLD);

        TextComponent intro = new TextComponent("New version available: ");
        intro.setColor(ChatColor.YELLOW);
        root.addExtra(intro);

        TextComponent latest = new TextComponent(latestVersion);
        latest.setColor(ChatColor.GREEN);
        root.addExtra(latest);

        TextComponent current = new TextComponent(" (actually " + currentVersion + ") ");
        current.setColor(ChatColor.GRAY);
        root.addExtra(current);

        TextComponent click = new TextComponent("[Click to open link]");
        click.setColor(ChatColor.AQUA);
        click.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, downloadUrl));
        click.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT, new TextComponent[] {new TextComponent("Open download page")}));
        root.addExtra(click);

        player.spigot().sendMessage(root);
    }
}
