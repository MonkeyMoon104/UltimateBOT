package com.monkey.ultimatebot.placeholders.list.combat;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.placeholders.IBotPlaceholder;
import com.monkey.ultimatebot.placeholders.PlaceholderHelper;
import org.bukkit.entity.Player;

public class HealingPlaceholder implements IBotPlaceholder {

    private final PlaceholderHelper helper;

    public HealingPlaceholder(UltimateBot plugin) {
        this.helper = new PlaceholderHelper(plugin);
    }

    @Override
    public String getIdentifier() {
        return "healing";
    }

    @Override
    public String getValue(Player player) {
        ITrainingBot bot = helper.getBotForPlaceholder(player);
        if (bot != null) {
            if (bot.getBotAI().getHealController().isHealing()) {
                return "♥ Active";
            } else if (bot.getBotAI().getHealController().shouldHeal()) {
                return "♥ Ready";
            } else {
                return "♥ Idle";
            }
        }
        return "♥ Unknown";
    }
}
