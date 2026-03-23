package com.monkey.mcbot.placeholders;

import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.bot.BotType;
import com.monkey.mcbot.bot.ai.ITrainingBot;
import org.bukkit.entity.Player;

public class PlaceholderHelper {

    private final MinecraftBot plugin;

    public PlaceholderHelper(MinecraftBot plugin) {
        this.plugin = plugin;
    }

    public ITrainingBot getBotForPlaceholder(Player player) {
        ITrainingBot eventBot = getActiveEventBot();
        if (eventBot != null) {
            return eventBot;
        }

        return plugin.getBotManager().getBotByParticipant(player.getUniqueId());
    }

    private ITrainingBot getActiveEventBot() {
        for (ITrainingBot bot : plugin.getBotRegistry().getAllBots().values()) {
            if (bot != null && bot.getBrainController() != null) {
                var botOptions = bot.getBrainController().getBotOptions();
                if (botOptions != null && botOptions.getBotType() == BotType.EVENT) {
                    return bot;
                }
            }
        }
        return null;
    }
}
