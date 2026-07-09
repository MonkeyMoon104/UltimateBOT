package com.monkey.mcbot.bot.ai.services;

import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.bot.BotOptions;
import com.monkey.mcbot.bot.BotType;
import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.nms.NMSBridgeManager;
import com.monkey.mcbot.utils.ChatColorUtils;
import com.monkey.mcbot.utils.armor.PlayerOptions;
import net.minecraft.world.damagesource.DamageSource;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

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
        BotOptions options = bot.getBrainController() != null ? bot.getBrainController().getBotOptions() : null;
        boolean isEventBot = options != null && options.getBotType() == BotType.EVENT;

        Player owner = getOwnerPlayer(options);

        if (isEventBot) {
            if (bot.getTargetPlayer() != null && bot.getTargetPlayer().isOnline()) {
                String targetName = bot.getTargetPlayer().getName();
                String translatedMsg = ChatColorUtils.translate(deadBotEventMessage.replace("{player}", targetName));
                Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(translatedMsg));
            }
        } else {
            if (owner != null && owner.isOnline()) {
                owner.sendMessage(ChatColorUtils.translate(deadBotMessage));
            } else if (bot.getTargetPlayer() != null && bot.getTargetPlayer().isOnline()) {
                bot.getTargetPlayer().sendMessage(ChatColorUtils.translate(deadBotMessage));
            }

            if (options != null && options.getBotType() == BotType.TEAM_ALLY) {
                for (UUID teamOwnerUUID : options.getTeamOwnerUUIDs()) {
                    playerOptions.remove(teamOwnerUUID);
                }
                if (options.getOwnerUUID() != null) {
                    playerOptions.remove(options.getOwnerUUID());
                }
            } else if (options != null && options.getOwnerUUID() != null) {
                playerOptions.remove(options.getOwnerUUID());
            } else if (bot.getTargetPlayer() != null) {
                playerOptions.remove(bot.getTargetPlayer().getUniqueId());
            }
        }

        bot.asPlayer().discard();
        NMSBridgeManager.get().removeFromProfileCache(bot.asPlayer().getUUID());
        plugin.forgetCompatibilityBot(bot.asPlayer().getUUID());
        plugin.getBotRegistry().removeBotByUUID(bot.asPlayer().getUUID());
    }

    private Player getOwnerPlayer(BotOptions options) {
        if (options == null) {
            return null;
        }

        UUID ownerUUID = options.getOwnerUUID();
        if (ownerUUID == null) {
            return null;
        }

        return Bukkit.getPlayer(ownerUUID);
    }
}
