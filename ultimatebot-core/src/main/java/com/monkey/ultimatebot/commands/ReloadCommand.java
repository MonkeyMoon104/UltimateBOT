package com.monkey.ultimatebot.commands;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.utils.ChatColorUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class ReloadCommand implements CommandExecutor, TabCompleter {

    private static final List<String> OPTIONS = Arrays.asList("config", "bot", "maps", "all");
    private final UltimateBot plugin;

    public ReloadCommand(UltimateBot plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("ultimatebot.admin.use")) {
            sender.sendMessage(ChatColorUtils.translate(plugin.getLangString("messages.reload-no-permission")));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColorUtils.translate(plugin.getLangString("messages.reload-usage")));
            return true;
        }

        String option = args[0].toLowerCase(Locale.ROOT);

        switch (option) {
            case "config" -> {
                reloadConfig();
                sender.sendMessage(ChatColorUtils.translate(plugin.getLangString("messages.reload-config")));
            }
            case "bot" -> {
                reloadBot();
                sender.sendMessage(ChatColorUtils.translate(plugin.getLangString("messages.reload-bot")));
            }
            case "maps" -> {
                reloadMaps();
                sender.sendMessage(ChatColorUtils.translate(plugin.getLangString("messages.reload-maps")));
            }
            case "all" -> {
                reloadAll();
                sender.sendMessage(ChatColorUtils.translate(plugin.getLangString("messages.reload-all")));
            }
            default ->
                sender.sendMessage(ChatColorUtils.translate(plugin.getLangString("messages.reload-invalid-option")));
        }

        return true;
    }

    public void reloadConfig() {
        plugin.reloadPluginConfiguration();
    }

    public void reloadBot() {
        plugin.getBotManager().despawnAll();
    }

    public void reloadMaps() {
        plugin.getPlayerOptions().clear();
    }

    public void reloadAll() {
        reloadConfig();
        reloadBot();
        reloadMaps();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            String toComplete = args[0].toLowerCase(Locale.ROOT);
            for (String option : OPTIONS) {
                if (option.startsWith(toComplete)) {
                    completions.add(option);
                }
            }
            return completions;
        }
        return List.of();
    }
}
