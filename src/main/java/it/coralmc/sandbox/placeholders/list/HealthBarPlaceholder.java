package it.coralmc.sandbox.placeholders.list;

import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.bot.ai.TrainingBot;
import it.coralmc.sandbox.placeholders.IBotPlaceholder;
import it.coralmc.sandbox.placeholders.PlaceholderHelper;
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
        TrainingBot bot = helper.getBotForPlaceholder(player);
        if (bot != null && bot.getBukkitEntity() != null) {
            double health = bot.getBukkitEntity().getHealth();
            double maxHealth = bot.getBukkitEntity().getMaxHealth();
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