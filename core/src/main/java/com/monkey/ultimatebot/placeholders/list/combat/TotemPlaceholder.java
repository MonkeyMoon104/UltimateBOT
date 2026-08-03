package com.monkey.ultimatebot.placeholders.list.combat;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.placeholders.IBotPlaceholder;
import com.monkey.ultimatebot.placeholders.PlaceholderHelper;
import org.bukkit.entity.Player;

public class TotemPlaceholder implements IBotPlaceholder {

    private final PlaceholderHelper helper;

    public TotemPlaceholder(UltimateBot plugin) {
        this.helper = new PlaceholderHelper(plugin);
    }

    @Override
    public String getIdentifier() {
        return "totems";
    }

    @Override
    public String getValue(Player player) {
        ITrainingBot bot = helper.getBotForPlaceholder(player);
        if (bot != null) {
            int count = bot.getTotemCount();
            return "✚ " + (count == -1 ? "Illimitati" : count);
        }
        return "✚ 0";
    }
}
