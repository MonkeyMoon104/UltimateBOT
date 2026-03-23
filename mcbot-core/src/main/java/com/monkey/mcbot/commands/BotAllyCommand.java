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

        if (isEventBotActive()) {
            String msg = plugin.getConfig().getString("messages.event-bot-active-block-normal", "&câŒ C'Ã¨ un bot event attivo! Non puoi spawnare bot normali durante un evento.");
            player.sendMessage(ChatColorUtils.translate(msg));
            return true;
        }

        if (hasSpawnedType(player, BotType.SINGLE) || plugin.getBotManager().hasActiveTeamAlly(player.getUniqueId())) {
            String msg = plugin.getConfig().getString("messages.cannot-open-botally-while-single", "&cHai gia un bot single spawnato. Despawnalo prima di usare /botally.");
            player.sendMessage(ChatColorUtils.translate(msg));
            return true;
        }

        new NewBotGUI(player, plugin, BotType.ALLY).open();
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

    private boolean hasSpawnedType(Player player, BotType type) {
        if (!plugin.getBotManager().isBotSpawned(player.getUniqueId())) {
            return false;
        }

        ITrainingBot bot = plugin.getBotManager().getBotSafe(player.getUniqueId());
        if (bot == null || bot.getBrainController() == null || bot.getBrainController().getBotOptions() == null) {
            return false;
        }

        return bot.getBrainController().getBotOptions().getBotType() == type;
    }
}