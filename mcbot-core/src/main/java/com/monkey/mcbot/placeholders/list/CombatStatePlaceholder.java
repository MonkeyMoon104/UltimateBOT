package com.monkey.mcbot.placeholders.list;

import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.bot.ai.BotAI;
import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.placeholders.IBotPlaceholder;
import com.monkey.mcbot.placeholders.PlaceholderHelper;
import org.bukkit.entity.Player;

public class CombatStatePlaceholder implements IBotPlaceholder {

    private final PlaceholderHelper helper;

    public CombatStatePlaceholder(MinecraftBot plugin) {
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
