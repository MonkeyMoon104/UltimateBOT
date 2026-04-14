package com.monkey.mcbot.commands;

import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.bot.BotType;
import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.gui.NewBotGUI;
import com.monkey.mcbot.utils.ChatColorUtils;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class BotCommand implements CommandExecutor {

    private final MinecraftBot plugin;

    public BotCommand(MinecraftBot plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        if (!sender.hasPermission("mcb.bot.use")) {
            sender.sendMessage(ChatColorUtils.translate(plugin.getLangString("messages.reload-no-permission", "")));
            return true;
        }

        World world = player.getWorld();
        List<String> blockedWorlds = plugin.getConfig().getStringList("bot.blocked-worlds");
        if (blockedWorlds.stream().anyMatch(blockedWorld -> blockedWorld.equalsIgnoreCase(world.getName()))) {
            String msg = plugin.getLangString("messages.bot-blocked-world", "&cYou cannot use this here!");
            player.sendMessage(ChatColorUtils.translate(msg));
            return true;
        }

        if (isEventBotActive()) {
            String msg = plugin.getLangString(
                    "messages.event-bot-active-block-normal",
                    "&cX An event bot is active! You cannot spawn normal bots during an event."
            );
            player.sendMessage(ChatColorUtils.translate(msg));
            return true;
        }

        BotType activeType = plugin.getBotManager().getBotTypeByParticipant(player.getUniqueId());
        if (activeType == BotType.ALLY || activeType == BotType.TEAM_ALLY) {
            player.sendMessage(ChatColorUtils.translate(getConflictMessage(activeType)));
            return true;
        }

        new NewBotGUI(player, plugin, BotType.SINGLE).open();
        return true;
    }

    private boolean isEventBotActive() {
        for (ITrainingBot bot : plugin.getBotRegistry().getAllBots().values()) {
            if (bot != null && bot.getBrainController() != null) {
                var botOptions = bot.getBrainController().getBotOptions();
                if (botOptions != null && botOptions.getBotType() == BotType.EVENT) {
                    return true;
                }
            }
        }
        return false;
    }

    private String getConflictMessage(BotType activeType) {
        if (activeType == BotType.ALLY) {
            return plugin.getLangString(
                    "messages.cannot-open-bot-while-ally",
                    "&cYou already have an ally bot spawned. Despawn it before using /bot."
            );
        }

        return "&cYou already have an active team ally bot. Despawn it before using /bot.";
    }
}
