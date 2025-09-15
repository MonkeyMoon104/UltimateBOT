package it.coralmc.sandbox.bot.ai.rank;

import it.coralmc.sandbox.utils.ChatColorUtils;

public enum BotRank {
    EASY("&7Easy", "&aEasy"),
    NORMAL("&7Normal", "&fNormal"),
    MEDIUM("&7Medium", "&eMedium"),
    HARD("&7Hard", "&cHard"),
    GOD("&7God", "&6&lGOD");

    private final String displayName;
    private final String selectedRankName;

    BotRank(String displayName, String selectedRankName) {
        this.displayName = displayName;
        this.selectedRankName = selectedRankName;
    }

    public String getSelectedName() {
        return ChatColorUtils.translate(selectedRankName);
    }

    public String getDisplayName() {
        return ChatColorUtils.translate(displayName);
    }

}
