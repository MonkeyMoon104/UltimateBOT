package com.monkey.ultimatebot.commands;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.BotType;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.compat.WorldAccess;
import com.monkey.ultimatebot.nms.NMSBridgeManager;
import com.monkey.ultimatebot.utils.ChatColorUtils;
import java.util.List;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

public class BotAllyCommand {

    private final UltimateBot plugin;

    public BotAllyCommand(UltimateBot plugin) {
        this.plugin = plugin;
    }

    @Command("botally")
    @CommandPermission(value = "ultimatebot.bot.use", defaultAccess = PermissionDefault.TRUE)
    public void botAlly(BukkitCommandActor actor) {
        if (!actor.isPlayer()) {
            return;
        }
        Player player = actor.requirePlayer();

        World world = player.getWorld();
        List<String> blockedWorlds = plugin.getConfig().getStringList("bot.blocked-worlds");
        if (blockedWorlds.stream().anyMatch(blockedWorld -> blockedWorld.equalsIgnoreCase(WorldAccess.name(world)))) {
            String msg = plugin.getLangString("messages.bot-blocked-world", "&cYou cannot use this here!");
            player.sendMessage(ChatColorUtils.translate(msg));
            return;
        }

        if (isEventBotActive()) {
            String msg = plugin.getLangString("messages.event-bot-active-block-normal");
            player.sendMessage(ChatColorUtils.translate(msg));
            return;
        }

        BotType activeType = plugin.getBotManager().getBotTypeByParticipant(player.getUniqueId());
        if (activeType == BotType.SINGLE || activeType == BotType.TEAM_ALLY) {
            player.sendMessage(ChatColorUtils.translate(getConflictMessage(activeType)));
            return;
        }

        if (!NMSBridgeManager.isBotRuntimeSupported()) {
            player.sendMessage(ChatColorUtils.translate(
                    "&cUltimateBot fake-player bots are not available on this Minecraft version in this build."));
            return;
        }

        NMSBridgeManager.get().openBotGui(player, plugin, BotType.ALLY);
    }

    private boolean isEventBotActive() {
        for (ITrainingBot bot : plugin.getBotRegistry().getAllBots().values()) {
            if (bot != null && bot.getBrainController() != null) {
                com.monkey.ultimatebot.bot.BotOptions botOptions = bot.getBrainController().getBotOptions();
                if (botOptions != null && botOptions.getBotType() == BotType.EVENT) {
                    return true;
                }
            }
        }
        return false;
    }

    private String getConflictMessage(BotType activeType) {
        if (activeType == BotType.SINGLE) {
            return plugin.getLangString(
                    "messages.cannot-open-botally-while-single",
                    "&cYou already have a single bot spawned. Despawn it before using /botally.");
        }

        return "&cYou already have an active team ally bot. Despawn it before using /botally.";
    }
}
