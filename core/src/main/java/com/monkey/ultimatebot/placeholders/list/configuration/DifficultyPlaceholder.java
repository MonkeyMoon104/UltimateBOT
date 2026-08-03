package com.monkey.ultimatebot.placeholders.list.configuration;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.difficulty.DifficultyLevel;
import com.monkey.ultimatebot.placeholders.IBotPlaceholder;
import com.monkey.ultimatebot.placeholders.PlaceholderHelper;
import org.bukkit.entity.Player;

public class DifficultyPlaceholder implements IBotPlaceholder {

    private final PlaceholderHelper helper;

    public DifficultyPlaceholder(UltimateBot plugin) {
        this.helper = new PlaceholderHelper(plugin);
    }

    @Override
    public String getIdentifier() {
        return "difficulty";
    }

    @Override
    public String getValue(Player player) {
        ITrainingBot bot = helper.getBotForPlaceholder(player);
        if (bot == null) return "● Offline";

        DifficultyLevel difficulty = bot.getBotAI().getDifficulty();
        if (difficulty == null) return "○ Unknown Difficulty";

        return switch (difficulty) {
            case EASY -> "● Easy";
            case NORMAL -> "◈ Normal";
            case MEDIUM -> "⚔ Medium";
            case HARD -> "▲ Hard";
            case GOD -> "☠ God";
        };
    }
}
