package it.coralmc.sandbox.placeholders.list;

import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.bot.ai.TrainingBot;
import it.coralmc.sandbox.placeholders.IBotPlaceholder;
import it.coralmc.sandbox.placeholders.PlaceholderHelper;
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