package it.coralmc.sandbox.commands.reload;

import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.commands.reload.options.ReloadPermission;
import it.coralmc.sandbox.commands.reload.options.ReloadService;
import it.coralmc.sandbox.utils.chatcolor.ChatColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {

    private final SandboxTraining plugin;
    private final ReloadService reloadService;
    private final ChatColorUtils chatColorUtils;

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
}