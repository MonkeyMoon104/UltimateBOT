package com.monkey.ultimatebot.placeholders.list.configuration;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.placeholders.IBotPlaceholder;
import com.monkey.ultimatebot.placeholders.PlaceholderHelper;
import org.bukkit.entity.Player;

public class FollowPlaceholder implements IBotPlaceholder {

    private final PlaceholderHelper helper;

    public FollowPlaceholder(UltimateBot plugin) {
        this.helper = new PlaceholderHelper(plugin);
    }

    @Override
    public String getIdentifier() {
        return "follow";
    }

    @Override
    public String getValue(Player player) {
        ITrainingBot bot = helper.getBotForPlaceholder(player);
        return bot != null ? (bot.isFollow() ? "✓" : "✗") : "✗";
    }
}
