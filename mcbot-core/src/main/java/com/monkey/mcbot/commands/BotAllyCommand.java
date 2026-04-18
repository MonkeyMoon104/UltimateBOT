package com.monkey.mcbot.commands;

import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.bot.BotType;
import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.nms.NMSBridgeManager;
import com.monkey.mcbot.utils.ChatColorUtils;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class BotAllyCommand implements CommandExecutor {

    private final MinecraftBot plugin;

    public BotAllyCommand(MinecraftBot plugin) {
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
            String msg = plugin.getLangString("messages.event-bot-active-block-normal");
            player.sendMessage(ChatColorUtils.translate(msg));
            return true;
        }

        BotType activeType = plugin.getBotManager().getBotTypeByParticipant(player.getUniqueId());
        if (activeType == BotType.SINGLE || activeType == BotType.TEAM_ALLY) {
            player.sendMessage(ChatColorUtils.translate(getConflictMessage(activeType)));
            return true;
        }

        NMSBridgeManager.get().openBotGui(player, plugin, BotType.ALLY);
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
        if (activeType == BotType.SINGLE) {
            return plugin.getLangString(
                    "messages.cannot-open-botally-while-single",
                    "&cYou already have a single bot spawned. Despawn it before using /botally."
            );
        }

        return "&cYou already have an active team ally bot. Despawn it before using /botally.";
    }
}
