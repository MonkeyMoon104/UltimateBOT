package com.monkey.mcbot.placeholders.list;

import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.placeholders.IBotPlaceholder;
import com.monkey.mcbot.placeholders.PlaceholderHelper;
import org.bukkit.entity.Player;

public class HealingPlaceholder implements IBotPlaceholder {

    private final MinecraftBot plugin;
    private final PlaceholderHelper helper;

    public HealingPlaceholder(MinecraftBot plugin) {
        this.plugin = plugin;
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