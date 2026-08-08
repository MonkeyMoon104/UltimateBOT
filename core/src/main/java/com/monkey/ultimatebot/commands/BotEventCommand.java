package com.monkey.ultimatebot.commands;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.BotType;
import com.monkey.ultimatebot.nms.NMSBridgeManager;
import com.monkey.ultimatebot.utils.ChatColorUtils;
import java.util.List;
import org.bukkit.World;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

public class BotEventCommand {

    private final UltimateBot plugin;

    public BotEventCommand(UltimateBot plugin) {
        this.plugin = plugin;
    }

    @Command("botevent")
    @CommandPermission("admin.host.bot")
    public void botEvent(BukkitCommandActor actor) {
        if (!actor.isPlayer()) {
            return;
        }
        Player player = actor.requirePlayer();

        World world = player.getWorld();
        List<String> blockedWorlds = plugin.getConfig().getStringList("bot.blocked-worlds");
        if (blockedWorlds.stream().anyMatch(blockedWorld -> blockedWorld.equalsIgnoreCase(world.getName()))) {
            String msg = plugin.getLangString("messages.bot-blocked-world", "&cYou cannot use this here!");
            player.sendMessage(ChatColorUtils.translate(msg));
            return;
        }

        BotType activeType = plugin.getBotManager().getBotTypeByParticipant(player.getUniqueId());
        if (activeType != null && activeType != BotType.EVENT) {
            player.sendMessage(ChatColorUtils.translate(getConflictMessage(activeType)));
            return;
        }

        NMSBridgeManager.get().openBotGui(player, plugin, BotType.EVENT);
    }

    private String getConflictMessage(BotType activeType) {
        if (activeType == BotType.SINGLE) {
            return plugin.getLangString(
                    "messages.must-despawn-normal-bot",
                    "&cX You already have a normal bot spawned! Despawn it before managing the event bot.");
        }

        if (activeType == BotType.ALLY) {
            return "&cX You already have an active ally bot. Despawn it before managing the event bot.";
        }

        return "&cX You already have an active team ally bot. Despawn it before managing the event bot.";
    }
}
