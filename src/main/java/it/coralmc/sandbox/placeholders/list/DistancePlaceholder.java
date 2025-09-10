package it.coralmc.sandbox.placeholders.list;

import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.bot.ai.TrainingBot;
import it.coralmc.sandbox.placeholders.IBotPlaceholder;
import org.bukkit.entity.Player;

public class DistancePlaceholder implements IBotPlaceholder {

    private final SandboxTraining plugin;

    public DistancePlaceholder(SandboxTraining plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "distance";
    }

    @Override
    public String getValue(Player player) {
        TrainingBot bot = plugin.getBot(player);
        if (bot != null && bot.getBukkitEntity() != null) {
            double distance = player.getLocation().distance(bot.getBukkitEntity().getLocation());
            return String.format("◈ %.1fm", distance);
        }
        return "◈ ∞";
    }
}