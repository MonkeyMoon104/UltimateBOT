package com.monkey.mcbot.placeholders.list;

import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.placeholders.IBotPlaceholder;
import com.monkey.mcbot.placeholders.PlaceholderHelper;
import org.bukkit.entity.Player;

public class CombatPlaceholder implements IBotPlaceholder {

    private final MinecraftBot plugin;
    private final PlaceholderHelper helper;

    public CombatPlaceholder(MinecraftBot plugin) {
        this.plugin = plugin;
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