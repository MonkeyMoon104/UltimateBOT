package com.monkey.mcbot.placeholders.list;

import com.monkey.mcbot.SandboxTraining;
import com.monkey.mcbot.bot.ai.TrainingBot;
import com.monkey.mcbot.placeholders.IBotPlaceholder;
import com.monkey.mcbot.placeholders.PlaceholderHelper;
import org.bukkit.entity.Player;

public class PercentageHealthPlaceholder implements IBotPlaceholder {

    private final SandboxTraining plugin;
    private final PlaceholderHelper helper;

    public PercentageHealthPlaceholder(SandboxTraining plugin) {
        this.plugin = plugin;
        this.helper = new PlaceholderHelper(plugin);
    }

    @Override
    public String getIdentifier() {
        return "health_percentage";
    }

    @Override
    public String getValue(Player player) {
        TrainingBot bot = helper.getBotForPlaceholder(player);
        if (bot != null && bot.getBukkitEntity() != null) {
            double health = bot.getBukkitEntity().getHealth();
            double maxHealth = bot.getBukkitEntity().getMaxHealth();
            int percentage = (int) Math.round((health / maxHealth) * 100);
            return "♥ " + percentage + "%";
        }
        return "♥ 0%";
    }
}