package com.monkey.ultimatebot.placeholders.list;

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
        return switch (state) {
            case AGGRESSIVE -> "AGGRESSIVE";
            case DEFENSIVE -> "DEFENSIVE";
            case REPOSITIONING -> "REPOSITIONING";
            case ANCHOR_SETUP -> "ANCHOR_SETUP";
            case CRYSTAL_SETUP -> "CRYSTAL_SETUP";
            case RETREATING -> "RETREATING";
            default -> "UNKNOWN";
        };
    }
}
