package it.coralmc.sandbox.placeholders.list;

import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.bot.ai.TrainingBot;
import it.coralmc.sandbox.placeholders.IBotPlaceholder;
import org.bukkit.entity.Player;

public class HealthPlaceholder implements IBotPlaceholder {

    private final SandboxTraining plugin;

    public HealthPlaceholder(SandboxTraining plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "health";
    }

    @Override
    public String getValue(Player player) {
        TrainingBot bot = plugin.getBot(player);
        if (bot != null && bot.getBukkitEntity() != null) {
            double health = bot.getBukkitEntity().getHealth();
            double maxHealth = bot.getBukkitEntity().getMaxHealth();
            return String.format("%.1f/%.1f", health, maxHealth);
        }
        return "0.0/20.0";
    }
}