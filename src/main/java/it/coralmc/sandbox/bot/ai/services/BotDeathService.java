package it.coralmc.sandbox.bot.ai.services;

import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.bot.ai.TrainingBot;
import it.coralmc.sandbox.utils.ChatColorUtils;
import it.coralmc.sandbox.utils.armor.PlayerOptions;
import net.minecraft.world.damagesource.DamageSource;
import org.bukkit.Bukkit;

public class BotDeathService {

    private final TrainingBot bot;
    private final SandboxTraining plugin;
    private final PlayerOptions playerOptions;
    private final String deadBotMessage;
    private final String deadBotEventMessage;

    public BotDeathService(TrainingBot bot, SandboxTraining plugin,
                           PlayerOptions playerOptions, String deadBotMessage, String deadBotEventMessage) {
        this.bot = bot;
        this.plugin = plugin;
        this.playerOptions = playerOptions;
        this.deadBotMessage = deadBotMessage;
        this.deadBotEventMessage = deadBotEventMessage;
    }

    public void handleDeath(DamageSource cause) {
        bot.getInventory().items.clear();

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

        bot.discard();
        plugin.getBotRegistry().removeBotByUUID(bot.getUUID());
    }
}