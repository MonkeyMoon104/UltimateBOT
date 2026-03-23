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
            String msg = plugin.getConfig().getString("messages.bot-blocked-world", "&cNon puoi qui!");
            player.sendMessage(ChatColorUtils.translate(msg));
            return true;
        }

        if (hasNormalBotSpawned(player)) {
            String msg = plugin.getConfig().getString("messages.must-despawn-normal-bot");
            player.sendMessage(ChatColorUtils.translate(msg));
            return true;
        }

        new NewBotGUI(player, plugin, BotType.EVENT).open();
        return true;
    }

    private boolean hasNormalBotSpawned(Player player) {
        if (plugin.getBotManager().hasActiveTeamAlly(player.getUniqueId())) {
            return true;
        }

        if (!plugin.getBotManager().isBotSpawned(player.getUniqueId())) {
            return false;
        }

        var bot = plugin.getBotManager().getBotSafe(player.getUniqueId());
        if (bot != null && bot.getBrainController() != null) {
            var botOptions = bot.getBrainController().getBotOptions();
            if (botOptions != null) {
                return botOptions.getBotType() != BotType.EVENT;
            }
        }
        return false;
    }
}