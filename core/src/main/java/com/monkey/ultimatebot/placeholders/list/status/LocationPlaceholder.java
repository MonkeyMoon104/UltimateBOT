package com.monkey.ultimatebot.placeholders.list.status;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.placeholders.IBotPlaceholder;
import com.monkey.ultimatebot.placeholders.PlaceholderHelper;
import org.bukkit.entity.Player;

public class LocationPlaceholder implements IBotPlaceholder {

    private final PlaceholderHelper helper;

    public LocationPlaceholder(UltimateBot plugin) {
        this.helper = new PlaceholderHelper(plugin);
    }

    @Override
    public String getIdentifier() {
        return "location";
    }

    @Override
    public String getValue(Player player) {
        ITrainingBot bot = helper.getBotForPlaceholder(player);
        if (bot != null && bot.asBukkitPlayer() != null) {
            org.bukkit.Location loc = java.util.Objects.requireNonNull(bot.asBukkitPlayer().getLocation(), "bot location");
            return String.format("⚬ X:%d Y:%d Z:%d", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        }
        return "⚬ X:0 Y:0 Z:0";
    }
}
