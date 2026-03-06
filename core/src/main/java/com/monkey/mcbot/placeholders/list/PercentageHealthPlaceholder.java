package com.monkey.mcbot.placeholders.list;

import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.placeholders.IBotPlaceholder;
import com.monkey.mcbot.placeholders.PlaceholderHelper;
import org.bukkit.entity.Player;

public class PercentageHealthPlaceholder implements IBotPlaceholder {

    private final MinecraftBot plugin;
    private final PlaceholderHelper helper;

    public PercentageHealthPlaceholder(MinecraftBot plugin) {
        this.plugin = plugin;
        this.helper = new PlaceholderHelper(plugin);
    }

    @Override
    public String getIdentifier() {
        return "health_percentage";
    }

    @Override
    public String getValue(Player player) {
        ITrainingBot bot = helper.getBotForPlaceholder(player);
        if (bot != null && bot.asPlayer().getBukkitEntity() != null) {
            double health = bot.asPlayer().getBukkitEntity().getHealth();
            double maxHealth = bot.asPlayer().getBukkitEntity().getMaxHealth();
            int percentage = (int) Math.round((health / maxHealth) * 100);
            return "♥ " + percentage + "%";
        }
        return "♥ 0%";
    }
}