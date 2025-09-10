package it.coralmc.sandbox.placeholders.list;

import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.bot.ai.TrainingBot;
import it.coralmc.sandbox.placeholders.IBotPlaceholder;
import org.bukkit.entity.Player;

public class HealingPlaceholder implements IBotPlaceholder {

    private final SandboxTraining plugin;

    public HealingPlaceholder(SandboxTraining plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "healing";
    }

    @Override
    public String getValue(Player player) {
        TrainingBot bot = plugin.getBot(player);
        if (bot != null) {
            if (bot.getBotAI().getHealController().isHealing()) {
                return "♥ Active";
            } else if (bot.getBotAI().getHealController().shouldHeal()) {
                return "♥ Ready";
            } else {
                return "♥ Idle";
            }
        }
        return "♥ Unknown";
    }
}