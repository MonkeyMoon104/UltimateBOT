package it.coralmc.sandbox.bot.ai.handlers;

import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.bot.ai.TrainingBot;
import it.coralmc.sandbox.utils.ChatColorUtils;
import it.coralmc.sandbox.utils.armor.PlayerOptions;
import net.minecraft.world.damagesource.DamageSource;

public class BotDeathHandler {

    private final TrainingBot bot;
    private final SandboxTraining plugin;
    private final PlayerOptions playerOptions;
    private final String deadBotMessage;

    public BotDeathHandler(TrainingBot bot, SandboxTraining plugin,
                           PlayerOptions playerOptions, String deadBotMessage) {
        this.bot = bot;
        this.plugin = plugin;
        this.playerOptions = playerOptions;
        this.deadBotMessage = deadBotMessage;
    }

    public void handleDeath(DamageSource cause) {
        bot.getInventory().items.clear();

        if (bot.getTargetPlayer() != null && bot.getTargetPlayer().isOnline()) {
            bot.getTargetPlayer().sendMessage(ChatColorUtils.translate(deadBotMessage));
            playerOptions.remove(bot.getTargetPlayer().getUniqueId());
        }

        bot.discard();
        plugin.getBotRegistry().removeBotByUUID(bot.getUUID());
    }
}
