package it.coralmc.sandbox.placeholders.list;

import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.bot.ai.TrainingBot;
import it.coralmc.sandbox.placeholders.IBotPlaceholder;
import org.bukkit.entity.Player;

public class StatusPlaceholder implements IBotPlaceholder {

    private final SandboxTraining plugin;

    public StatusPlaceholder(SandboxTraining plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "status";
    }

    @Override
    public String getValue(Player player) {
        TrainingBot bot = plugin.getBot(player);
        if (bot == null) return "🔴 Offline";

        if (bot.isCombat()) return "⚔️ Combat";
        if (bot.isFollow()) return "👥 Following";
        return "🟢 Idle";
    }
}