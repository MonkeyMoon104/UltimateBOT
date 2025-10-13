package it.coralmc.sandbox.placeholders.list;

import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.bot.ai.BotAI;
import it.coralmc.sandbox.bot.ai.TrainingBot;
import it.coralmc.sandbox.placeholders.IBotPlaceholder;
import it.coralmc.sandbox.placeholders.PlaceholderHelper;
import org.bukkit.entity.Player;

public class CombatStatePlaceholder implements IBotPlaceholder {

    private final SandboxTraining plugin;
    private final PlaceholderHelper helper;

    public CombatStatePlaceholder(SandboxTraining plugin) {
        this.plugin = plugin;
        this.helper = new PlaceholderHelper(plugin);
    }

    @Override
    public String getIdentifier() {
        return "combat_state";
    }

    @Override
    public String getValue(Player player) {
        TrainingBot bot = helper.getBotForPlaceholder(player);
        if (bot == null || !bot.isCombat()) return "None";

        BotAI.CombatState state = bot.getBotAI().getCurrentState();
        return switch (state) {
            case AGGRESSIVE -> "AGGRESSIVE";
            case DEFENSIVE -> "DEFENSIVE";
            case REPOSITIONING -> "REPOSITIONING";
            case ANCHOR_SETUP -> "ANCHOR_SETUP";
            case CRYSTAL_SETUP -> "CRYSTAL_SETUP";
            case RETREATING -> "RETREATING";
            default -> "UNKNOWN";
        };
    }
}