package com.monkey.mcbot.placeholders.list;

import com.monkey.mcbot.SandboxTraining;
import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.bot.ai.rank.BotRank;
import com.monkey.mcbot.placeholders.IBotPlaceholder;
import com.monkey.mcbot.placeholders.PlaceholderHelper;
import org.bukkit.entity.Player;

public class RankPlaceholder implements IBotPlaceholder {

    private final SandboxTraining plugin;
    private final PlaceholderHelper helper;

    public RankPlaceholder(SandboxTraining plugin) {
        this.plugin = plugin;
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
