package com.monkey.mcbot.placeholders.list;

import com.monkey.mcbot.SandboxTraining;
import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.placeholders.IBotPlaceholder;
import com.monkey.mcbot.placeholders.PlaceholderHelper;
import org.bukkit.entity.Player;

public class LocationPlaceholder implements IBotPlaceholder {

    private final SandboxTraining plugin;
    private final PlaceholderHelper helper;

    public LocationPlaceholder(SandboxTraining plugin) {
        this.plugin = plugin;
        this.helper = new PlaceholderHelper(plugin);
    }

    @Override
    public String getIdentifier() {
        return "location";
    }

    @Override
    public String getValue(Player player) {
        ITrainingBot bot = helper.getBotForPlaceholder(player);
        if (bot != null && bot.asPlayer().getBukkitEntity() != null) {
            var loc = bot.asPlayer().getBukkitEntity().getLocation();
            return String.format("⚬ X:%d Y:%d Z:%d",
                    loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        }
        return "⚬ X:0 Y:0 Z:0";
    }
}