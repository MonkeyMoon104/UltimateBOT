package com.monkey.ultimatebot.api;

import com.monkey.ultimatebot.api.addon.AddonRegistry;
import com.monkey.ultimatebot.api.event.bus.BotEventBus;
import com.monkey.ultimatebot.api.extension.UltimateBotExtensionRegistry;
import com.monkey.ultimatebot.api.managers.IBotManager;
import com.monkey.ultimatebot.api.managers.IBotRegistry;
import com.monkey.ultimatebot.common.model.platform.PlatformCapability;
import com.monkey.ultimatebot.common.model.platform.PlatformInfo;
import com.monkey.ultimatebot.common.model.settings.ServerConfiguration;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Logger;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.Nullable;

/**
 * Main public entry point for the UltimateBot API.
 *
 * <p>This object exposes bot management, snapshots, events, dynamic extensions and addon status.
 * {@link IBotManager} handles operations and {@link IBotRegistry} provides read-only snapshots.
 * The instance is registered by the core plugin during startup and can be retrieved by
 * external plugins after {@code UltimateBotReadyEvent} is fired.</p>
 *
 * <p>Typical usage in another plugin:</p>
 * <pre>{@code
 * UltimateBotAPI api = UltimateBotAPI.getOrNull();
 * if (api != null) {
 *     IBotManager manager = api.getBotManager();
 * }
 * }</pre>
 */
public final class UltimateBotAPI {

    private static final Logger LOGGER = Logger.getLogger("UltimateBot/API");
    private static volatile @Nullable UltimateBotAPI instance;

    private final Plugin plugin;
    private final IBotManager botManager;
    private final IBotRegistry botRegistry;
    private final BotEventBus eventBus;
    private final UltimateBotExtensionRegistry extensions;
    private final AddonRegistry addons;
    private final Supplier<ServerConfiguration> serverConfiguration;

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
     * @param extensions dynamic combat-mode and brain registry
     * @param addons read-only hosted-addon registry
     * @param serverConfiguration supplier for current server-wide plugin settings
     */
    public UltimateBotAPI(
            Plugin plugin,
            IBotManager botManager,
            IBotRegistry botRegistry,
            UltimateBotExtensionRegistry extensions,
            AddonRegistry addons,
            Supplier<ServerConfiguration> serverConfiguration) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.botManager = Objects.requireNonNull(botManager, "botManager");
        this.botRegistry = Objects.requireNonNull(botRegistry, "botRegistry");
        this.extensions = Objects.requireNonNull(extensions, "extensions");
        this.addons = Objects.requireNonNull(addons, "addons");
        this.serverConfiguration = Objects.requireNonNull(serverConfiguration, "serverConfiguration");
        this.eventBus = new BotEventBus();
    }

    /**
     * Registers the global API singleton.
     *
     * <p>Calling this method replaces any previous instance.</p>
     *
     * @param api fully initialized API instance
     */
    public static void register(UltimateBotAPI api) {
        instance = Objects.requireNonNull(api, "api");
        LOGGER.info(() -> "UltimateBotAPI registered. plugin=" + api.plugin.getName()
                + ", botManager=bound"
                + ", botRegistry=bound");
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
    public static UltimateBotAPI get() {
        UltimateBotAPI current = instance;
        if (current == null) {
            throw new IllegalStateException("UltimateBotAPI is not available yet.");
        }
        return current;
    }

    /**
     * Returns the registered API singleton when available.
     *
     * @return API instance, or {@code null} if not registered
     */
    public static @Nullable UltimateBotAPI getOrNull() {
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

    /** Returns the typed Bukkit-backed event subscription facade. */
    public BotEventBus getEventBus() {
        return eventBus;
    }

    /** Returns the dynamic combat-mode and custom-brain registry. */
    public UltimateBotExtensionRegistry getExtensions() {
        return extensions;
    }

    /** Returns the read-only status of jars managed by the addon engine. */
    public AddonRegistry getAddons() {
        return addons;
    }

    /** Returns the current server-wide UltimateBot configuration snapshot. */
    public ServerConfiguration getServerConfiguration() {
        return serverConfiguration.get();
    }

    /** Returns the loaded Minecraft version and NMS feature flags for this server. */
    public PlatformInfo getPlatform() {
        return botManager.getPlatform();
    }

    /** Returns the NMS feature flags of the loaded server bridge. */
    public Set<PlatformCapability> getPlatformCapabilities() {
        return getPlatform().capabilities();
    }

    /** Returns whether the loaded NMS bridge exposes the given platform feature. */
    public boolean supports(PlatformCapability capability) {
        return botManager.supports(capability);
    }
}
