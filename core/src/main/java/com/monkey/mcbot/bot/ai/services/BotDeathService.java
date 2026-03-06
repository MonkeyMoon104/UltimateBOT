package com.monkey.mcbot.bot.ai.services;

import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.utils.ChatColorUtils;
import com.monkey.mcbot.utils.armor.PlayerOptions;
import net.minecraft.world.damagesource.DamageSource;
import org.bukkit.Bukkit;

public class BotDeathService {

    private final ITrainingBot bot;
    private final MinecraftBot plugin;
    private final PlayerOptions playerOptions;
    private final String deadBotMessage;
    private final String deadBotEventMessage;

    public BotDeathService(ITrainingBot bot, MinecraftBot plugin,
                           PlayerOptions playerOptions, String deadBotMessage, String deadBotEventMessage) {
        this.bot = bot;
        this.plugin = plugin;
        this.playerOptions = playerOptions;
        this.deadBotMessage = deadBotMessage;
        this.deadBotEventMessage = deadBotEventMessage;
    }

    public void handleDeath(DamageSource cause) {
        boolean isEventBot = bot.getBrainController() != null &&
                bot.getBrainController().getBotOptions() != null &&
                bot.getBrainController().getBotOptions().isEventBot();

        if (bot.getTargetPlayer() != null && bot.getTargetPlayer().isOnline()) {
            String targetName = bot.getTargetPlayer().getName();

            if (isEventBot) {
                String translatedMsg = ChatColorUtils.translate(deadBotEventMessage.replace("{player}", targetName));
                Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(translatedMsg));
            } else {
                bot.getTargetPlayer().sendMessage(ChatColorUtils.translate(deadBotMessage));
                playerOptions.remove(bot.getTargetPlayer().getUniqueId());
            }
        }

        bot.asPlayer().discard();
        plugin.getBotRegistry().removeBotByUUID(bot.asPlayer().getUUID());
    }
}