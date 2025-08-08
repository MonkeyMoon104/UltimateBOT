package it.coralmc.sandbox.commands.reload;

import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.commands.reload.options.ReloadPermission;
import it.coralmc.sandbox.commands.reload.options.ReloadService;
import it.coralmc.sandbox.utils.chatcolor.ChatColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReloadCommand implements CommandExecutor, TabCompleter {

    private final SandboxTraining plugin;
    private final ReloadService reloadService;
    private final ChatColorUtils chatColorUtils;
    private static final List<String> OPTIONS = Arrays.asList("config", "bot", "maps", "all");

    public ReloadCommand(SandboxTraining plugin) {
        this.plugin = plugin;
        this.reloadService = new ReloadService(plugin);
        this.chatColorUtils = plugin.getChatColorUtils();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission(ReloadPermission.ADMIN_USE)) {
            sender.sendMessage(chatColorUtils.translate(plugin.getConfig().getString("messages.reload-no-permission")));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(chatColorUtils.translate(plugin.getConfig().getString("messages.reload-usage")));
            return true;
        }

        String option = args[0].toLowerCase();

        switch (option) {
            case "config" -> {
                reloadService.reloadConfig();
                sender.sendMessage(chatColorUtils.translate(plugin.getConfig().getString("messages.reload-config")));
            }
            case "bot" -> {
                reloadService.reloadBot();
                sender.sendMessage(chatColorUtils.translate(plugin.getConfig().getString("messages.reload-bot")));
            }
            case "maps" -> {
                reloadService.reloadMaps();
                sender.sendMessage(chatColorUtils.translate(plugin.getConfig().getString("messages.reload-maps")));
            }
            case "all" -> {
                reloadService.reloadAll();
                sender.sendMessage(chatColorUtils.translate(plugin.getConfig().getString("messages.reload-all")));
            }
            default -> sender.sendMessage(chatColorUtils.translate(plugin.getConfig().getString("messages.reload-invalid-option")));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            String toComplete = args[0].toLowerCase();
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