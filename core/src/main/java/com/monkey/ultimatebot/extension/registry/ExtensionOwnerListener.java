package com.monkey.ultimatebot.extension.registry;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import java.util.Objects;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;

public final class ExtensionOwnerListener implements Listener {
    private final UltimateBot plugin;
    private final CoreExtensionRegistry registry;

    public ExtensionOwnerListener(UltimateBot plugin, CoreExtensionRegistry registry) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        registry.unregisterOwner(event.getPlugin().getName());
        for (ITrainingBot bot : plugin.getBotRegistry().getAllBots().values()) {
            Player player = bot.asBukkitPlayer();
            if (player == null) {
                continue;
            }
            plugin.getWrapperManager().active().runEntity(player, bot.getBotAI()::refreshExtensions);
        }
    }
}
