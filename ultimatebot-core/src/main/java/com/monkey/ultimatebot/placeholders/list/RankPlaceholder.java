package com.monkey.ultimatebot.placeholders.list;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.rank.BotRank;
import com.monkey.ultimatebot.placeholders.IBotPlaceholder;
import com.monkey.ultimatebot.placeholders.PlaceholderHelper;
import org.bukkit.entity.Player;

public class RankPlaceholder implements IBotPlaceholder {

    private final PlaceholderHelper helper;

    public RankPlaceholder(UltimateBot plugin) {
        this.helper = new PlaceholderHelper(plugin);
    }

    @Override
    public String getIdentifier() {
        return "rank";
    }

    @Override
    public String getValue(Player player) {
        ITrainingBot bot = helper.getBotForPlaceholder(player);
        if (bot == null) return "● Offline";

        BotRank rank = bot.getBotAI().getRank();
        if (rank == null) return "○ Unknown Rank";

        return switch (rank) {
            case EASY -> "● Easy";
            case NORMAL -> "◈ Normal";
            case MEDIUM -> "⚔ Medium";
            case HARD -> "▲ Hard";
            case GOD -> "☠ God";
        };
    }
}
