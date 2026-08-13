package com.monkey.ultimatebot.commands;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.utils.ChatColorUtils;
import java.util.Locale;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.Suggest;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

public class ReloadCommand {

    private final UltimateBot plugin;

    public ReloadCommand(UltimateBot plugin) {
        this.plugin = plugin;
    }

    @Command("ultimatebotreload")
    @CommandPermission("ultimatebot.admin.use")
    public void reload(
            BukkitCommandActor actor,
            @Optional @Suggest({"config", "bot", "maps", "all"}) String option) {
        if (option == null || option.trim().isEmpty()) {
            actor.sender()
                    .sendMessage(ChatColorUtils.translate(plugin.getLangString("messages.reload-usage")));
            return;
        }

                switch (option.toLowerCase(Locale.ROOT)) {
            case "config":
                reloadConfig();
                                actor.sender()
                                        .sendMessage(ChatColorUtils.translate(plugin.getLangString("messages.reload-config")));
                break;
            case "bot":
                reloadBot();
                                actor.sender()
                                        .sendMessage(ChatColorUtils.translate(plugin.getLangString("messages.reload-bot")));
                break;
            case "maps":
                reloadMaps();
                                actor.sender()
                                        .sendMessage(ChatColorUtils.translate(plugin.getLangString("messages.reload-maps")));
                break;
            case "all":
                reloadAll();
                                actor.sender()
                                        .sendMessage(ChatColorUtils.translate(plugin.getLangString("messages.reload-all")));
                break;
            default:
                actor.sender()
                        .sendMessage(
                                ChatColorUtils.translate(plugin.getLangString("messages.reload-invalid-option")));
                break;
        }
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
}
