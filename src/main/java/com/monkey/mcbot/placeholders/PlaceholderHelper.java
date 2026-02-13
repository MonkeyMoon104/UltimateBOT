package com.monkey.mcbot.placeholders;

import com.monkey.mcbot.SandboxTraining;
import com.monkey.mcbot.bot.ai.TrainingBot;
import org.bukkit.entity.Player;

public class PlaceholderHelper {

    private final SandboxTraining plugin;

    public PlaceholderHelper(SandboxTraining plugin) {
        this.plugin = plugin;
    }

    public TrainingBot getBotForPlaceholder(Player player) {
        TrainingBot eventBot = getActiveEventBot();
        if (eventBot != null) {
            return eventBot;
        }

        return plugin.getBot(player);
    }

    private TrainingBot getActiveEventBot() {
        for (TrainingBot bot : plugin.getBotRegistry().getAllBots().values()) {
            if (bot != null && bot.getBrainController() != null) {
                var botOptions = bot.getBrainController().getBotOptions();
                if (botOptions != null && botOptions.isEventBot()) {
                    return bot;
                }
            }
        }
        return null;
    }
}