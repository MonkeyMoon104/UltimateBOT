package com.monkey.mcbot.placeholders.list;

import com.monkey.mcbot.SandboxTraining;
import com.monkey.mcbot.bot.ai.BotAI;
import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.placeholders.IBotPlaceholder;
import com.monkey.mcbot.placeholders.PlaceholderHelper;
import org.bukkit.entity.Player;

public class StatusPlaceholder implements IBotPlaceholder {

    private final SandboxTraining plugin;
    private final PlaceholderHelper helper;

    public StatusPlaceholder(SandboxTraining plugin) {
        this.plugin = plugin;
        this.helper = new PlaceholderHelper(plugin);
    }

    @Override
    public String getIdentifier() {
        return "status";
    }

    @Override
    public String getValue(Player player) {
        ITrainingBot bot = helper.getBotForPlaceholder(player);
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