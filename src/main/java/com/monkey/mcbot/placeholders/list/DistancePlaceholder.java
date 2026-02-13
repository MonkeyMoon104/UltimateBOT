package com.monkey.mcbot.placeholders.list;

import com.monkey.mcbot.SandboxTraining;
import com.monkey.mcbot.bot.ai.TrainingBot;
import com.monkey.mcbot.placeholders.IBotPlaceholder;
import com.monkey.mcbot.placeholders.PlaceholderHelper;
import org.bukkit.entity.Player;

public class DistancePlaceholder implements IBotPlaceholder {

    private final SandboxTraining plugin;
    private final PlaceholderHelper helper;

    public DistancePlaceholder(SandboxTraining plugin) {
        this.plugin = plugin;
        this.helper = new PlaceholderHelper(plugin);
    }

    @Override
    public String getIdentifier() {
        return "distance";
    }

    @Override
    public String getValue(Player player) {
        TrainingBot bot = helper.getBotForPlaceholder(player);
        if (bot != null && bot.getBukkitEntity() != null) {
            double distance = player.getLocation().distance(bot.getBukkitEntity().getLocation());
            return String.format("◈ %.1fm", distance);
        }
        return "◈ ∞";
    }
}