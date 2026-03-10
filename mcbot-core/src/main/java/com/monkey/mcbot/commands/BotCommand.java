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

        if (!sender.hasPermission("sb.bot.use")) {
            sender.sendMessage(ChatColorUtils.translate(plugin.getConfig().getString("messages.reload-no-permission", "")));
            return true;
        }

        World world = player.getWorld();
        if (world.getName().equalsIgnoreCase("SBLobby") || world.getName().equalsIgnoreCase("SBBox")) {
            player.sendRichMessage("<red>Non puoi qui!");
            return true;
        }

        if (isEventBotActive()) {
            String msg = plugin.getConfig().getString("messages.event-bot-active-block-normal", "&câŒ C'Ã¨ un bot event attivo! Non puoi spawnare bot normali durante un evento.");
            player.sendMessage(ChatColorUtils.translate(msg));
            return true;
        }

        if (hasSpawnedType(player, BotType.ALLY) || plugin.getBotManager().hasActiveTeamAlly(player.getUniqueId())) {
            String msg = plugin.getConfig().getString("messages.cannot-open-bot-while-ally", "&cHai gia un bot ally spawnato. Despawnalo prima di usare /bot.");
            player.sendMessage(ChatColorUtils.translate(msg));
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