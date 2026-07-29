package com.monkey.mcbot;

import com.monkey.mcbot.api.MinecraftBotAPI;
import com.monkey.mcbot.api.event.MinecraftBotReadyEvent;
import com.monkey.mcbot.bot.BotManager;
import com.monkey.mcbot.bot.BotRegistry;
import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.bot.ai.services.TargetingService;
import com.monkey.mcbot.commands.*;
import com.monkey.mcbot.integration.api.CoreBotManagerAdapter;
import com.monkey.mcbot.integration.api.CoreBotRegistryAdapter;
import com.monkey.mcbot.integration.worldguard.WorldGuardPvpService;
import com.monkey.mcbot.lang.LanguageManager;
import com.monkey.mcbot.license.LicenseManager;
import com.monkey.mcbot.license.LicenseStartupResult;
import com.monkey.mcbot.listener.BotGuardCompatibilityListener;
import com.monkey.mcbot.listener.BotExplosionListener;
import com.monkey.mcbot.listener.PlayerCheckListener;
import com.monkey.mcbot.listener.PlayerTagListener;
import com.monkey.mcbot.logging.MinecraftBotLogging;
import com.monkey.mcbot.nms.NMSBridgeManager;
import com.monkey.mcbot.placeholders.PlaceholderApiSupport;
import com.monkey.mcbot.placeholders.PlaceholderRegistration;
import com.monkey.mcbot.remote.RemoteApiServer;
import com.monkey.mcbot.update.UpdateManager;
import com.monkey.mcbot.update.UpdateStartupResult;
import com.monkey.mcbot.utils.armor.PlayerOptions;
import com.monkey.mcbot.wrapper.WrapperManager;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public final class MinecraftBot extends JavaPlugin {

    private static final int BSTATS_PLUGIN_ID = 30749;

    private PlayerOptions playerOptions;
    private BotRegistry botRegistry;
    private BotManager botManager;
    private PlaceholderRegistration placeholderCoordinator;
    private TargetingService targetingService;
    private LicenseManager licenseManager;
    private UpdateManager updateManager;
    private LanguageManager languageManager;
    private WrapperManager wrapperManager;
    private BotGuardCompatibilityListener botGuardCompatibilityListener;
    private WorldGuardPvpService worldGuardPvpService;
    private RemoteApiServer remoteApiServer;
    private static MinecraftBot instance;

    @Override
    public void onEnable() {
        instance = this;
        this.wrapperManager = new WrapperManager(this);
        MinecraftBotLogging.StartupSession startup = MinecraftBotLogging.beginBootstrap(this);

        try {
            startup.beginPhase(1, "Boot", "Configuration");
            saveDefaultConfig();
            reloadLanguageConfiguration();
            startup.ready("Config", "default file verified");
            startup.detail("Path", getDataFolder().toPath().resolve("config.yml").toString());
            startup.detail("Language", languageManager.getActiveLanguageFileName());
            startup.detail(
                    "Profile",
                    getConfig().getString("bot.name", "CrystalBot")
                            + " | default totems=" + getConfig().getInt("bot.default-totem-count")
                            + ", normal max=" + getConfig().getInt("bot.max-totem-normal")
                            + ", event max=" + getConfig().getInt("bot.max-totem-event")
            );
            startup.completePhase("config ready");

            startup.beginPhase(2, "License", "Validation");
            this.licenseManager = new LicenseManager(this);
            LicenseStartupResult licenseResult = licenseManager.validateOnStartup();
            if (!licenseResult.allowed()) {
                startup.warn("License", licenseResult.reasonCode() + " -> " + licenseResult.message());
                throw new IllegalStateException("License validation failed: " + licenseResult.reasonCode());
            }
            if (licenseResult.graceMode()) {
                startup.warn("License", licenseResult.message());
            } else {
                startup.ready("License", licenseResult.message());
            }
            startup.completePhase("license ready");

            startup.beginPhase(3, "Update", "Availability");
            this.updateManager = new UpdateManager(this);
            UpdateStartupResult updateResult = updateManager.checkOnStartup();
            if (updateResult.checkFailed()) {
                startup.warn("Update", updateResult.message());
            } else if (updateResult.updateAvailable()) {
                startup.warn("Update", updateResult.message());
            } else {
                startup.ready("Update", updateResult.message());
            }
            startup.completePhase("update check completed");

            startup.beginPhase(4, "NMS", "Compatibility");
            NMSBridgeManager.init(getLogger());
            startup.markNmsBridge(NMSBridgeManager.get().getClass().getSimpleName(), NMSBridgeManager.getSupportedVersions());
            startup.detail("Catalog", MinecraftBotLogging.catalogSummary());
            startup.detail("Controllers", MinecraftBotLogging.controllerSummary());
            startup.completePhase("server compatibility resolved");

            startup.beginPhase(5, "Core", "Runtime");
            this.targetingService = new TargetingService();
            this.playerOptions = new PlayerOptions();
            this.botRegistry = new BotRegistry();
            this.worldGuardPvpService = new WorldGuardPvpService(this);
            this.botManager = new BotManager(this);
            startup.ready("Runtime", "services created");
            startup.detail("Services", "TargetingService, PlayerOptions, BotRegistry, BotManager, WrapperManager, WorldGuardPvpService");
            startup.detail("Caches", "bots=" + botRegistry.size() + " | playerOptions=" + playerOptions.size());
            startup.detail("Wrappers", wrapperManager.describeActiveWrapper());
            startup.completePhase("runtime core created");

            startup.beginPhase(6, "API", "Wiring");
            MinecraftBotAPI api = new MinecraftBotAPI(
                    this,
                    new CoreBotManagerAdapter(this, botManager, botRegistry, playerOptions),
                    new CoreBotRegistryAdapter(botRegistry)
            );
            startup.ready("API", "adapters wired");
            startup.detail("Manager", MinecraftBotLogging.apiManagerSummary());
            startup.detail("Registry", MinecraftBotLogging.apiRegistrySummary());
            startup.completePhase("public API prepared");

            startup.beginPhase(7, "Hooks", "Commands and Listeners");

            List<String> registeredCommands = new ArrayList<>();
            registerCommand(startup, registeredCommands, "bot", new BotCommand(this));
            registerCommand(startup, registeredCommands, "botevent", new BotEventCommand(this));
            registerCommand(startup, registeredCommands, "botally", new BotAllyCommand(this));

            BotTeamAllyCommand botTeamAllyCommand = new BotTeamAllyCommand(this);
            registerCommand(startup, registeredCommands, "botteamally", botTeamAllyCommand, botTeamAllyCommand);
            registerCommand(startup, registeredCommands, "mcbreload", new ReloadCommand(this));
            startup.markCommands(registeredCommands);

            List<String> registeredListeners = new ArrayList<>();
            List<String> disabledListeners = new ArrayList<>();
            this.botGuardCompatibilityListener = new BotGuardCompatibilityListener(this);
            registerListener(startup, registeredListeners, "bot guard compatibility", botGuardCompatibilityListener);
            botGuardCompatibilityListener.startScanner();
            registerListener(startup, registeredListeners, "bot explosion protection", new BotExplosionListener());
            registerListener(startup, registeredListeners, "required", new PlayerCheckListener(this));
            registerOptionalListener(
                    startup,
                    registeredListeners,
                    disabledListeners,
                    "optional integration",
                    this::isCombatLogXListenerAvailable,
                    "CombatLogX dependency unavailable",
                    () -> new PlayerTagListener(this)
            );
            startup.markListeners(registeredListeners, disabledListeners);
            startup.ready("Hooks", "registrations completed");
            startup.detail("Commands", joinOrNone(registeredCommands));
            startup.detail("Listeners", startup.listenerStateCountSummary());
            if (!disabledListeners.isEmpty()) {
                startup.detail("Listener fallback", joinOrNone(disabledListeners));
            }
            startup.completePhase("hooks registered");

            startup.beginPhase(8, "PAPI", "Placeholder Integration");
            if (PlaceholderApiSupport.isAvailable()) {
                this.placeholderCoordinator = PlaceholderApiSupport.createRegistration(this);
                if (this.placeholderCoordinator != null) {
                    boolean placeholderRegistered = placeholderCoordinator.register();
                    List<String> placeholderKeys = placeholderCoordinator.getRegisteredPlaceholderKeys();

                    startup.markPlaceholders(true, placeholderRegistered, placeholderKeys);
                    startup.detail("Namespace", placeholderCoordinator.getIdentifier());
                    startup.detail("Placeholders", placeholderKeys.size() + " -> " + joinOrNone(placeholderKeys));

                    if (placeholderRegistered) {
                        startup.ready("PlaceholderAPI", "hook attached");
                    } else {
                        startup.warn("PlaceholderAPI", "hook failed");
                    }
                } else {
                    startup.markPlaceholders(true, false, List.of());
                    startup.warn("PlaceholderAPI", "hook initialization failed");
                }
            } else {
                startup.markPlaceholders(false, false, List.of());
                startup.warn("PlaceholderAPI", "not found");
            }
            startup.completePhase("placeholder phase completed");

            startup.beginPhase(9, "Boot", "Finalize");
            MinecraftBotAPI.register(api);
            startup.markApiPublished();
            MinecraftBotLogging.logApiRegistered(getLogger(), api);
            getServer().getPluginManager().callEvent(new MinecraftBotReadyEvent(api));
            startup.ready("Event", MinecraftBotReadyEvent.class.getSimpleName() + " fired");
            this.remoteApiServer = new RemoteApiServer(this, api);
            this.remoteApiServer.start();

            try {
                Metrics metrics = new Metrics(this, BSTATS_PLUGIN_ID);
                metrics.addCustomChart(new SimplePie("plugin_version", () -> getPluginMeta().getVersion()));
                startup.ready("bStats", "metrics enabled");
            } catch (Throwable metricsError) {
                startup.warn("bStats", "metrics init failed -> " + formatListenerError(metricsError));
            }

            if (licenseManager != null) {
                licenseManager.startHeartbeat();
                startup.ready("License", "heartbeat started");
            }
            if (updateManager != null) {
                updateManager.startRuntime();
                startup.ready("Update", "runtime notifications enabled");
            }
            startup.completePhase("enable sequence completed");

            startup.completeBootstrap();
            MinecraftBotLogging.schedulePostEnableDiagnostics(this, startup);
        } catch (Throwable error) {
            startup.fail(error);
            cleanupRuntimeState();
            throw error;
        }
    }

    @Override
    public void onDisable() {
        cleanupRuntimeState();
        instance = null;
    }

    public ITrainingBot getBot(Player player) {
        if (botManager == null || !botManager.isBotSpawned(player.getUniqueId())) {
            return null;
        }
        return botManager.getBotSafe(player.getUniqueId());
    }

    public BotRegistry getBotRegistry() {
        return botRegistry;
    }

    public BotManager getBotManager() {
        return botManager;
    }

    public TargetingService getTargetingService() {
        return targetingService;
    }

    public PlayerOptions getPlayerOptions() {
        return playerOptions;
    }

    public static MinecraftBot getInstance() {
        return instance;
    }

    public WrapperManager getWrapperManager() {
        return wrapperManager;
    }

    public WorldGuardPvpService getWorldGuardPvpService() {
        return worldGuardPvpService;
    }

    public void markCompatibilityBot(Entity entity) {
        if (botGuardCompatibilityListener != null) {
            botGuardCompatibilityListener.markBot(entity);
        }
    }

    public void forgetCompatibilityBot(UUID entityUUID) {
        if (botGuardCompatibilityListener != null) {
            botGuardCompatibilityListener.forgetBot(entityUUID);
        }
    }

    public void reloadPluginConfiguration() {
        reloadConfig();
        reloadLanguageConfiguration();
        if (botGuardCompatibilityListener != null) {
            botGuardCompatibilityListener.reloadLocalConfig();
            botGuardCompatibilityListener.startScanner();
        }
        applyRuntimeBotConfiguration();
    }

    public void reloadLanguageConfiguration() {
        if (languageManager == null) {
            languageManager = new LanguageManager(this);
        }
        languageManager.reload();
    }

    public String getLangString(String path) {
        if (languageManager == null) {
            return getConfig().getString(path);
        }
        return languageManager.getString(path);
    }

    public String getLangString(String path, String fallbackValue) {
        if (languageManager == null) {
            return getConfig().getString(path, fallbackValue);
        }
        return languageManager.getString(path, fallbackValue);
    }

    public List<String> getLangStringList(String path) {
        if (languageManager == null) {
            return getConfig().contains(path) ? getConfig().getStringList(path) : Collections.emptyList();
        }
        return languageManager.getStringList(path);
    }

    public FileConfiguration getLanguageConfig() {
        if (languageManager == null) {
            return getConfig();
        }
        return languageManager.getActiveLanguageConfiguration();
    }

    private void registerCommand(MinecraftBotLogging.StartupSession startup,
                                 List<String> registeredCommands,
                                 String name,
                                 CommandExecutor executor) {
        registerCommand(startup, registeredCommands, name, executor, null);
    }

    private void registerCommand(MinecraftBotLogging.StartupSession startup,
                                 List<String> registeredCommands,
                                 String name,
                                 CommandExecutor executor,
                                 TabCompleter tabCompleter) {
        PluginCommand command = getCommand(name);
        if (command == null) {
            startup.warn("Command /" + name, "missing from plugin.yml");
            return;
        }

        command.setExecutor(executor);
        if (tabCompleter != null) {
            command.setTabCompleter(tabCompleter);
        }

        registeredCommands.add(name);
    }

    private void registerListener(MinecraftBotLogging.StartupSession startup,
                                  List<String> registeredListeners,
                                  String label,
                                  Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
        registeredListeners.add(label);
    }

    private void registerOptionalListener(MinecraftBotLogging.StartupSession startup,
                                          List<String> registeredListeners,
                                          List<String> disabledListeners,
                                          String label,
                                          Supplier<Boolean> availabilityCheck,
                                          String unavailableReason,
                                          Supplier<? extends Listener> listenerSupplier) {
        if (!availabilityCheck.get()) {
            disabledListeners.add(label + " -> " + unavailableReason);
            startup.warn("Listener " + label, "disabled -> " + unavailableReason);
            return;
        }

        try {
            registerListener(startup, registeredListeners, label, listenerSupplier.get());
        } catch (Throwable error) {
            String reason = "registration failed: " + formatListenerError(error);
            disabledListeners.add(label + " -> " + reason);
            startup.warn("Listener " + label, "disabled -> " + reason);
        }
    }

    private boolean isCombatLogXListenerAvailable() {
        return getServer().getPluginManager().getPlugin("CombatLogX") != null
                && hasRuntimeClass("com.github.sirblobman.combatlogx.api.event.PlayerPreTagEvent");
    }

    private boolean hasRuntimeClass(String className) {
        try {
            Class.forName(className, false, getClassLoader());
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private String formatListenerError(Throwable error) {
        String message = error.getMessage();
        if (message == null || message.isBlank()) {
            return error.getClass().getSimpleName();
        }
        return error.getClass().getSimpleName() + " -> " + message;
    }

    private void cleanupRuntimeState() {
        if (remoteApiServer != null) {
            remoteApiServer.stop();
            remoteApiServer = null;
        }

        if (botGuardCompatibilityListener != null) {
            botGuardCompatibilityListener.stopScanner();
            botGuardCompatibilityListener = null;
        }

        if (updateManager != null) {
            updateManager.shutdown();
            updateManager = null;
        }

        if (licenseManager != null) {
            licenseManager.shutdown();
            licenseManager = null;
        }

        MinecraftBotAPI.unregister();

        if (placeholderCoordinator != null) {
            placeholderCoordinator.unregister();
            placeholderCoordinator = null;
        }
        if (botManager != null) {
            botManager.despawnAll();
            botManager = null;
        }
        if (botRegistry != null) {
            botRegistry.clear();
            botRegistry = null;
        }
        if (playerOptions != null) {
            playerOptions.clear();
            playerOptions = null;
        }
        targetingService = null;
        worldGuardPvpService = null;
        languageManager = null;
        wrapperManager = null;
    }

    private void applyRuntimeBotConfiguration() {
        if (botRegistry == null) {
            return;
        }

        boolean infiniteResources = getConfig().getBoolean("bot.combat.infinite-resources", true);
        for (ITrainingBot bot : botRegistry.getAllBots().values()) {
            if (bot != null && bot.getBotAI() != null) {
                bot.getBotAI().getInventoryController().setInfiniteResources(infiniteResources);
            }
        }
    }

    private String joinOrNone(Iterable<String> values) {
        if (values == null) {
            return "none";
        }
        String joined = String.join(", ", values);
        return joined.isBlank() ? "none" : joined;
    }
}
