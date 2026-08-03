package com.monkey.ultimatebot.placeholders.list.status;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.placeholders.IBotPlaceholder;
import com.monkey.ultimatebot.placeholders.PlaceholderHelper;
import org.bukkit.entity.Player;

public class HealthPlaceholder implements IBotPlaceholder {

    private final PlaceholderHelper helper;

    public HealthPlaceholder(UltimateBot plugin) {
        this.helper = new PlaceholderHelper(plugin);
    }

    @Override
    public String getIdentifier() {
        return "health";
    }

    @Override
    public String getValue(Player player) {
        ITrainingBot bot = helper.getBotForPlaceholder(player);
        if (bot != null && bot.asPlayer().getBukkitEntity() != null) {
            double health = bot.asPlayer().getBukkitEntity().getHealth();
            double maxHealth = bot.asPlayer().getBukkitEntity().getMaxHealth();
            return String.format("♥ %.1f/%.1f", health, maxHealth);
        }
        return "♥ 0.0/20.0";
    }
}
