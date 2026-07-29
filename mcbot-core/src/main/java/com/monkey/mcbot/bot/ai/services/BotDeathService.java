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
import com.monkey.mcbot.api.event.BotDeathEvent;
import com.monkey.mcbot.api.event.BotDespawnEvent;
import com.monkey.mcbot.api.event.BotDespawnReason;
import com.monkey.mcbot.api.event.BotEventSource;
import com.monkey.mcbot.api.model.BotSnapshot;

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
        UUID ownerUUID = options != null ? options.getOwnerUUID() : plugin.getBotRegistry().getOwnerUUIDByBotUUID(bot.asPlayer().getUUID());
        BotSnapshot deathSnapshot = ownerUUID == null ? null : plugin.getBotEventDispatcher().snapshot(ownerUUID, bot);
        if (deathSnapshot != null) {
            org.bukkit.entity.Entity killer = cause == null || cause.getEntity() == null
                    ? null : cause.getEntity().getBukkitEntity();
            plugin.getBotEventDispatcher().publish(new BotDeathEvent(
                    plugin.getBotEventDispatcher().nextSequence(bot.asPlayer().getUUID()),
                    deathSnapshot, cause == null ? "unknown" : cause.typeHolder().getRegisteredName(), killer));
        }

        if (isEventBot) {
            if (isKillMessageEnabled(options) && bot.getTargetPlayer() != null && bot.getTargetPlayer().isOnline()) {
                String targetName = bot.getTargetPlayer().getName();
                String template = resolveKillMessage(options, deadBotEventMessage);
                String translatedMsg = ChatColorUtils.translate(template.replace("{player}", targetName));
                Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(translatedMsg));
            }
        } else {
            if (isKillMessageEnabled(options)) {
                String message = ChatColorUtils.translate(resolveKillMessage(options, deadBotMessage));
                if (owner != null && owner.isOnline()) {
                    owner.sendMessage(message);
                } else if (bot.getTargetPlayer() != null && bot.getTargetPlayer().isOnline()) {
                    bot.getTargetPlayer().sendMessage(message);
                }
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
        if (deathSnapshot != null) {
            plugin.getBotEventDispatcher().publish(new BotDespawnEvent(
                    plugin.getBotEventDispatcher().nextSequence(bot.asPlayer().getUUID()), deathSnapshot,
                    BotEventSource.SYSTEM, BotDespawnReason.BOT_DEATH));
            plugin.getBotEventDispatcher().forget(bot.asPlayer().getUUID());
        }
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

    private boolean isKillMessageEnabled(BotOptions options) {
        return options == null || options.isKillMessageEnabled();
    }

    private String resolveKillMessage(BotOptions options, String fallback) {
        if (options != null && options.getCustomKillMessage() != null) {
            return options.getCustomKillMessage();
        }
        return fallback == null ? "" : fallback;
    }
}
