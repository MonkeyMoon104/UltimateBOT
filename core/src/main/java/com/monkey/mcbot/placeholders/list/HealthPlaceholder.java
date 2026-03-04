package com.monkey.mcbot.placeholders.list;

import com.monkey.mcbot.SandboxTraining;
import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.placeholders.IBotPlaceholder;
import com.monkey.mcbot.placeholders.PlaceholderHelper;
import org.bukkit.entity.Player;

public class HealthPlaceholder implements IBotPlaceholder {

    private final SandboxTraining plugin;
    private final PlaceholderHelper helper;

    public HealthPlaceholder(SandboxTraining plugin) {
        this.plugin = plugin;
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