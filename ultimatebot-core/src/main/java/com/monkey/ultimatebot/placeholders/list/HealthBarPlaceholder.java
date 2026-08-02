package com.monkey.ultimatebot.placeholders.list;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.placeholders.IBotPlaceholder;
import com.monkey.ultimatebot.placeholders.PlaceholderHelper;
import org.bukkit.entity.Player;

public class HealthBarPlaceholder implements IBotPlaceholder {

    private final PlaceholderHelper helper;

    public HealthBarPlaceholder(UltimateBot plugin) {
        this.helper = new PlaceholderHelper(plugin);
    }

    @Override
    public String getIdentifier() {
        return "health_bar";
    }

    @Override
    public String getValue(Player player) {
        ITrainingBot bot = helper.getBotForPlaceholder(player);
        if (bot != null && bot.asPlayer().getBukkitEntity() != null) {
            double health = bot.asPlayer().getBukkitEntity().getHealth();
            double maxHealth = bot.asPlayer().getBukkitEntity().getMaxHealth();
            double percentage = health / maxHealth;

            int filledBars = (int) Math.round(percentage * 10);
            StringBuilder bar = new StringBuilder();

            for (int i = 0; i < 10; i++) {
                if (i < filledBars) {
                    bar.append("█");
                } else {
                    bar.append("░");
                }
            }

            return bar.toString();
        }
        return "░░░░░░░░░░";
    }
}
