package com.monkey.mcbot.placeholders.list;

import com.monkey.mcbot.SandboxTraining;
import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.placeholders.IBotPlaceholder;
import com.monkey.mcbot.placeholders.PlaceholderHelper;
import org.bukkit.entity.Player;

public class HealthBarPlaceholder implements IBotPlaceholder {

    private final SandboxTraining plugin;
    private final PlaceholderHelper helper;

    public HealthBarPlaceholder(SandboxTraining plugin) {
        this.plugin = plugin;
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