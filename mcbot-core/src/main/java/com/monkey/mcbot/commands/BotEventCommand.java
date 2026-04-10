package com.monkey.mcbot.commands;

import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.bot.BotType;
import com.monkey.mcbot.gui.NewBotGUI;
import com.monkey.mcbot.utils.ChatColorUtils;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class BotEventCommand implements CommandExecutor {

    private final MinecraftBot plugin;

    public BotEventCommand(MinecraftBot plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        if (!sender.hasPermission("admin.host.bot")) {
            sender.sendMessage(ChatColorUtils.translate(plugin.getConfig().getString("messages.reload-no-permission", "")));
            return true;
        }

        World world = player.getWorld();
        List<String> blockedWorlds = plugin.getConfig().getStringList("bot.blocked-worlds");
        if (blockedWorlds.stream().anyMatch(blockedWorld -> blockedWorld.equalsIgnoreCase(world.getName()))) {
            String msg = plugin.getConfig().getString("messages.bot-blocked-world", "&cYou cannot use this here!");
            player.sendMessage(ChatColorUtils.translate(msg));
            return true;
        }

        BotType activeType = plugin.getBotManager().getBotTypeByParticipant(player.getUniqueId());
        if (activeType != null && activeType != BotType.EVENT) {
            player.sendMessage(ChatColorUtils.translate(getConflictMessage(activeType)));
            return true;
        }

        new NewBotGUI(player, plugin, BotType.EVENT).open();
        return true;
    }

    private String getConflictMessage(BotType activeType) {
        if (activeType == BotType.SINGLE) {
            return plugin.getConfig().getString(
                    "messages.must-despawn-normal-bot",
                    "&cX You already have a normal bot spawned! Despawn it before managing the event bot."
            );
        }

        if (activeType == BotType.ALLY) {
            return "&cX You already have an active ally bot. Despawn it before managing the event bot.";
        }

        return "&cX You already have an active team ally bot. Despawn it before managing the event bot.";
    }
}
