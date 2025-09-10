package it.coralmc.sandbox.placeholders.list;

import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.bot.ai.BotAI;
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
        if (bot == null) return "● Offline";

        if (bot.getBotAI().getHealController().isHealing()) return "♥ Healing";
        if (bot.isCombat()) {
            BotAI.CombatState state = bot.getBotAI().getCurrentState();
            return switch (state) {
                case AGGRESSIVE -> "⚔ Aggressive";
                case DEFENSIVE -> "◈ Defensive";
                case REPOSITIONING -> "↗ Repositioning";
                case ANCHOR_SETUP -> "▲ Anchor Setup";
                case CRYSTAL_SETUP -> "◊ Crystal Setup";
                case RETREATING -> "← Retreating";
                default -> "⚔ Combat";
            };
        }
        if (bot.isFollow()) return "● Following";
        return "○ Idle";
    }
}