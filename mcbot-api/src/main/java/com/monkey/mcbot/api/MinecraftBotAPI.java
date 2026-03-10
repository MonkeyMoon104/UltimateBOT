package com.monkey.mcbot.api;

import com.monkey.mcbot.api.managers.IBotManager;
import com.monkey.mcbot.api.managers.IBotRegistry;
import org.bukkit.plugin.Plugin;

import java.util.Objects;
import java.util.logging.Logger;

public final class MinecraftBotAPI {

    private static final Logger LOGGER = Logger.getLogger("MinecraftBot/API");
    private static volatile MinecraftBotAPI instance;

    private final Plugin plugin;
    private final IBotManager botManager;
    private final IBotRegistry botRegistry;

    public MinecraftBotAPI(Plugin plugin, IBotManager botManager, IBotRegistry botRegistry) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.botManager = Objects.requireNonNull(botManager, "botManager");
        this.botRegistry = Objects.requireNonNull(botRegistry, "botRegistry");
    }

    public static void register(MinecraftBotAPI api) {
        instance = Objects.requireNonNull(api, "api");
        LOGGER.info(() -> "MinecraftBotAPI registered. plugin=" + api.plugin.getName()
                + ", botManager=" + api.botManager.getClass().getSimpleName()
                + ", botRegistry=" + api.botRegistry.getClass().getSimpleName());
    }

    public static void unregister() {
        instance = null;
    }

    public static MinecraftBotAPI get() {
        MinecraftBotAPI current = instance;
        if (current == null) {
            throw new IllegalStateException("MinecraftBotAPI is not available yet.");
        }
        return current;
    }

    public static MinecraftBotAPI getOrNull() {
        return instance;
    }

    public static boolean isAvailable() {
        return instance != null;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public IBotManager getBotManager() {
        return botManager;
    }

    public IBotRegistry getBotRegistry() {
        return botRegistry;
    }
}
