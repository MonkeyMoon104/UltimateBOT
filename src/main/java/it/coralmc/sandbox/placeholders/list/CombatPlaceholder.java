package it.coralmc.sandbox.placeholders.list;

import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.bot.ai.TrainingBot;
import it.coralmc.sandbox.placeholders.IBotPlaceholder;
import org.bukkit.entity.Player;

public class CombatPlaceholder implements IBotPlaceholder {

    private final SandboxTraining plugin;

    public CombatPlaceholder(SandboxTraining plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "combat";
    }

    @Override
    public String getValue(Player player) {
        TrainingBot bot = plugin.getBot(player);
        return bot != null ? (bot.isCombat() ? "⚔" : "●") : "○";
    }
}