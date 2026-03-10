package com.monkey.mcbot.api;

import com.monkey.mcbot.api.managers.IBotManager;
import com.monkey.mcbot.api.managers.IBotRegistry;
import org.bukkit.plugin.Plugin;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * Main public entry point for the MinecraftBot API.
 *
 * <p>This object exposes the two core API services:
 * {@link IBotManager} for operations and {@link IBotRegistry} for read-only snapshots.
 * The instance is registered by the core plugin during startup and can be retrieved by
 * external plugins after {@code MinecraftBotReadyEvent} is fired.</p>
 *
 * <p>Typical usage in another plugin:</p>
 * <pre>{@code
 * MinecraftBotAPI api = MinecraftBotAPI.getOrNull();
 * if (api != null) {
 *     IBotManager manager = api.getBotManager();
 *     // spawn / update / remove operations
 * }
 * }</pre>
 */
public final class MinecraftBotAPI {

    private static final Logger LOGGER = Logger.getLogger("MinecraftBot/API");
    private static volatile MinecraftBotAPI instance;

    private final Plugin plugin;
    private final IBotManager botManager;
    private final IBotRegistry botRegistry;

    /**
     * Creates a new API container.
     *
     * <p>This constructor is intended for the core plugin integration layer.
     * External plugins should not instantiate this class directly; they should
     * access the registered singleton through {@link #get()} or {@link #getOrNull()}.</p>
     *
     * @param plugin owning Bukkit plugin instance
     * @param botManager bot management service implementation
     * @param botRegistry bot registry service implementation
     */
    public MinecraftBotAPI(Plugin plugin, IBotManager botManager, IBotRegistry botRegistry) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.botManager = Objects.requireNonNull(botManager, "botManager");
        this.botRegistry = Objects.requireNonNull(botRegistry, "botRegistry");
    }

    /**
     * Registers the global API singleton.
     *
     * <p>Calling this method replaces any previous instance.</p>
     *
     * @param api fully initialized API instance
     */
    public static void register(MinecraftBotAPI api) {
        instance = Objects.requireNonNull(api, "api");
        LOGGER.info(() -> "MinecraftBotAPI registered. plugin=" + api.plugin.getName()
                + ", botManager=" + api.botManager.getClass().getSimpleName()
                + ", botRegistry=" + api.botRegistry.getClass().getSimpleName());
    }

    /**
     * Unregisters the global API singleton.
     *
     * <p>After this call, {@link #isAvailable()} returns {@code false} and {@link #get()}
     * throws {@link IllegalStateException} until a new registration occurs.</p>
     */
    public static void unregister() {
        instance = null;
    }

    /**
     * Returns the registered API singleton.
     *
     * @return non-null API instance
     * @throws IllegalStateException if the API is not registered yet
     */
    public static MinecraftBotAPI get() {
        MinecraftBotAPI current = instance;
        if (current == null) {
            throw new IllegalStateException("MinecraftBotAPI is not available yet.");
        }
        return current;
    }

    /**
     * Returns the registered API singleton when available.
     *
     * @return API instance, or {@code null} if not registered
     */
    public static MinecraftBotAPI getOrNull() {
        return instance;
    }

    /**
     * Indicates whether the API singleton is currently available.
     *
     * @return {@code true} if {@link #get()} can be safely called
     */
    public static boolean isAvailable() {
        return instance != null;
    }

    /**
     * Returns the Bukkit plugin that owns this API instance.
     *
     * @return owning plugin
     */
    public Plugin getPlugin() {
        return plugin;
    }

    /**
     * Returns the operational bot manager service.
     *
     * @return bot manager implementation
     */
    public IBotManager getBotManager() {
        return botManager;
    }

    /**
     * Returns the registry service for reading bot snapshots.
     *
     * @return bot registry implementation
     */
    public IBotRegistry getBotRegistry() {
        return botRegistry;
    }
}
