package com.monkey.ultimatebot.placeholders;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.BotType;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

public class PlaceholderHelper {

    private final UltimateBot plugin;

    public PlaceholderHelper(UltimateBot plugin) {
        this.plugin = plugin;
    }

    public @Nullable ITrainingBot getBotForPlaceholder(Player player) {
        ITrainingBot eventBot = getActiveEventBot();
        if (eventBot != null) {
            return eventBot;
        }

        return plugin.getBotManager().getBotByParticipant(player.getUniqueId());
    }

    private @Nullable ITrainingBot getActiveEventBot() {
        for (ITrainingBot bot : plugin.getBotRegistry().getAllBots().values()) {
            if (bot != null && bot.getBrainController() != null) {
                com.monkey.ultimatebot.bot.BotOptions botOptions =
                        bot.getBrainController().getBotOptions();
                if (botOptions != null && botOptions.getBotType() == BotType.EVENT) {
                    return bot;
                }
            }
        }
        return null;
    }
}
