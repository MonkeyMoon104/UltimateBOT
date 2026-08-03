package com.monkey.ultimatebot.placeholders.list.status;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.placeholders.IBotPlaceholder;
import com.monkey.ultimatebot.placeholders.PlaceholderHelper;
import org.bukkit.entity.Player;

public class OnlineTimePlaceholder implements IBotPlaceholder {

    private final PlaceholderHelper helper;

    public OnlineTimePlaceholder(UltimateBot plugin) {
        this.helper = new PlaceholderHelper(plugin);
    }

    @Override
    public String getIdentifier() {
        return "online_time";
    }

    @Override
    public String getValue(Player player) {
        ITrainingBot bot = helper.getBotForPlaceholder(player);
        if (bot != null && bot.asPlayer().getBukkitEntity() != null) {
            long ticks = bot.asPlayer().tickCount;
            long seconds = ticks / 20;
            long minutes = seconds / 60;
            long hours = minutes / 60;

            if (hours > 0) {
                return String.format("⌚ %dh %dm", hours, minutes % 60);
            } else if (minutes > 0) {
                return String.format("⌚ %dm %ds", minutes, seconds % 60);
            } else {
                return String.format("⌚ %ds", seconds);
            }
        }
        return "⌚ 0s";
    }
}
