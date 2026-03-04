package com.monkey.mcbot.placeholders.list;

import com.monkey.mcbot.SandboxTraining;
import com.monkey.mcbot.placeholders.IBotPlaceholder;
import com.monkey.mcbot.bot.ai.ITrainingBot;
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
        ITrainingBot bot = helper.getBotForPlaceholder(player);
        if (bot != null && bot.asPlayer().getBukkitEntity() != null) {
            double distance = player.getLocation().distance(bot.asPlayer().getBukkitEntity().getLocation());
            return String.format("◈ %.1fm", distance);
        }
        return "◈ ∞";
    }
}