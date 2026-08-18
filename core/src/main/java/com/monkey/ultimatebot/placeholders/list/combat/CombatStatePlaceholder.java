package com.monkey.ultimatebot.placeholders.list.combat;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.ai.BotAI;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.placeholders.IBotPlaceholder;
import com.monkey.ultimatebot.placeholders.PlaceholderHelper;
import org.bukkit.entity.Player;

public class CombatStatePlaceholder implements IBotPlaceholder {

    private final PlaceholderHelper helper;

    public CombatStatePlaceholder(UltimateBot plugin) {
        this.helper = new PlaceholderHelper(plugin);
    }

    @Override
    public String getIdentifier() {
        return "combat_state";
    }

    @Override
    public String getValue(Player player) {
        ITrainingBot bot = helper.getBotForPlaceholder(player);
        if (bot == null || !bot.isCombat()) return "None";

        BotAI.CombatState state = bot.getBotAI().getCurrentState();
        switch (state) {
            case AGGRESSIVE:
                return "AGGRESSIVE";
            case DEFENSIVE:
                return "DEFENSIVE";
            case REPOSITIONING:
                return "REPOSITIONING";
            case ANCHOR_SETUP:
                return "ANCHOR_SETUP";
            case CRYSTAL_SETUP:
                return "CRYSTAL_SETUP";
            case RETREATING:
                return "RETREATING";
            default:
                return "UNKNOWN";
        }
    }
}
