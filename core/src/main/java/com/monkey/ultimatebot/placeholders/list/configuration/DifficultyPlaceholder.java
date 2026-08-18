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

        switch (difficulty) {
            case EASY:
                return "● Easy";
            case NORMAL:
                return "◈ Normal";
            case MEDIUM:
                return "⚔ Medium";
            case HARD:
                return "▲ Hard";
            case GOD:
                return "☠ God";
        }
        throw new IllegalStateException("Unexpected switch value");
    }
}
