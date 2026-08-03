package com.monkey.ultimatebot.bot.ai.difficulty;

import com.monkey.ultimatebot.utils.ChatColorUtils;

public enum DifficultyLevel {
    EASY("&7Easy", "&aEasy"),
    NORMAL("&7Normal", "&1Normal"),
    MEDIUM("&7Medium", "&eMedium"),
    HARD("&7Hard", "&cHard"),
    GOD("&7God", "&6&lGOD");

    private final String displayName;
    private final String selectedDifficultyName;

    DifficultyLevel(String displayName, String selectedDifficultyName) {
        this.displayName = displayName;
        this.selectedDifficultyName = selectedDifficultyName;
    }

    public String getSelectedName() {
        return ChatColorUtils.translate(selectedDifficultyName);
    }

    public String getDisplayName() {
        return ChatColorUtils.translate(displayName);
    }
}
