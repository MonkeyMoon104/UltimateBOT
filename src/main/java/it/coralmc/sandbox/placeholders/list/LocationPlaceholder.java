package it.coralmc.sandbox.placeholders.list;

import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.bot.ai.TrainingBot;
import it.coralmc.sandbox.placeholders.IBotPlaceholder;
import org.bukkit.entity.Player;

public class LocationPlaceholder implements IBotPlaceholder {

    private final SandboxTraining plugin;

    public LocationPlaceholder(SandboxTraining plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "location";
    }

    @Override
    public String getValue(Player player) {
        TrainingBot bot = plugin.getBot(player);
        if (bot != null && bot.getBukkitEntity() != null) {
            var loc = bot.getBukkitEntity().getLocation();
            return String.format("⚬ X:%d Y:%d Z:%d",
                    loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        }
        return "⚬ X:0 Y:0 Z:0";
    }
}