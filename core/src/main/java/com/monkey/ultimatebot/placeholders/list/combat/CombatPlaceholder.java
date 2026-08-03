package com.monkey.ultimatebot.placeholders.list.combat;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.placeholders.IBotPlaceholder;
import com.monkey.ultimatebot.placeholders.PlaceholderHelper;
import org.bukkit.entity.Player;

public class CombatPlaceholder implements IBotPlaceholder {

    private final PlaceholderHelper helper;

    public CombatPlaceholder(UltimateBot plugin) {
        this.helper = new PlaceholderHelper(plugin);
    }

    @Override
    public String getIdentifier() {
        return "combat";
    }

    @Override
    public String getValue(Player player) {
        ITrainingBot bot = helper.getBotForPlaceholder(player);
        return bot != null ? (bot.isCombat() ? "⚔" : "●") : "○";
    }
}
