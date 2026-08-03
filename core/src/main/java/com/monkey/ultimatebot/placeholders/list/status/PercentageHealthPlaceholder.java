package com.monkey.ultimatebot.placeholders.list.status;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.placeholders.IBotPlaceholder;
import com.monkey.ultimatebot.placeholders.PlaceholderHelper;
import org.bukkit.entity.Player;

public class PercentageHealthPlaceholder implements IBotPlaceholder {

    private final PlaceholderHelper helper;

    public PercentageHealthPlaceholder(UltimateBot plugin) {
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
