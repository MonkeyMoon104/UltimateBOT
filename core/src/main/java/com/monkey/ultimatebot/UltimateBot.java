package com.monkey.ultimatebot;

import com.monkey.ultimatebot.addon.GuardAddonManager;
import com.monkey.ultimatebot.addon.runtime.CoreAddonRegistry;
import com.monkey.ultimatebot.addon.runtime.UltimateBotAddonEngine;
import com.monkey.ultimatebot.api.UltimateBotAPI;
import com.monkey.ultimatebot.api.event.lifecycle.BotDespawnReason;
import com.monkey.ultimatebot.api.event.lifecycle.UltimateBotReadyEvent;
import com.monkey.ultimatebot.bot.BotManager;
import com.monkey.ultimatebot.bot.BotRegistry;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.services.TargetingService;
import com.monkey.ultimatebot.combat.profile.CombatProfileCatalog;
import com.monkey.ultimatebot.commands.*;
import com.monkey.ultimatebot.common.model.CombatMode;
import com.monkey.ultimatebot.access.runtime.PluginMetaAccess;
import com.monkey.ultimatebot.config.CombatProfileLoader;
import com.monkey.ultimatebot.config.ConfigurateRuntimeSettingsLoader;
import com.monkey.ultimatebot.config.RuntimeSettings;
import com.monkey.ultimatebot.event.BotEventDispatcher;
import com.monkey.ultimatebot.extension.registry.CoreExtensionRegistry;
import com.monkey.ultimatebot.extension.registry.ExtensionOwnerListener;
import com.monkey.ultimatebot.integration.api.CoreBotManagerAdapter;
import com.monkey.ultimatebot.integration.api.CoreBotRegistryAdapter;
import com.monkey.ultimatebot.integration.worldguard.WorldGuardPvpService;
import com.monkey.ultimatebot.lang.LanguageManager;
import com.monkey.ultimatebot.license.LicenseManager;
import com.monkey.ultimatebot.license.LicenseStartupResult;
import com.monkey.ultimatebot.listener.BotExplosionListener;
import com.monkey.ultimatebot.listener.BotRuntimeEventListener;
import com.monkey.ultimatebot.listener.PlayerCheckListener;
import com.monkey.ultimatebot.listener.PlayerTagListener;
import com.monkey.ultimatebot.metrics.BotMetrics;
import com.monkey.ultimatebot.nms.NMSBridgeManager;
import com.monkey.ultimatebot.placeholders.PlaceholderApiSupport;
import com.monkey.ultimatebot.placeholders.PlaceholderRegistration;
import com.monkey.ultimatebot.remote.RemoteApiServer;
import com.monkey.ultimatebot.update.UpdateManager;
import com.monkey.ultimatebot.update.UpdateStartupResult;
import com.monkey.ultimatebot.utils.armor.PlayerOptions;
import com.monkey.ultimatebot.world.WorldProtectionListener;
import com.monkey.ultimatebot.world.WorldProtectionService;
import com.monkey.ultimatebot.wrapper.WrapperManager;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.Nullable;
import revxrsal.commands.bukkit.BukkitLamp;
import revxrsal.commands.bukkit.BukkitLampConfig;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

public final class UltimateBot extends JavaPlugin {

    private static final int BSTATS_PLUGIN_ID = 30749;

    private @Nullable PlayerOptions playerOptions;
    private @Nullable BotRegistry botRegistry;
    private @Nullable BotManager botManager;
    private @Nullable PlaceholderRegistration placeholderCoordinator;
    private @Nullable TargetingService targetingService;
    private @Nullable LicenseManager licenseManager;
    private @Nullable UpdateManager updateManager;
    private @Nullable LanguageManager languageManager;
    private @Nullable WrapperManager wrapperManager;
    private @Nullable GuardAddonManager guardAddonManager;
    private @Nullable WorldGuardPvpService worldGuardPvpService;
    private @Nullable WorldProtectionService worldProtectionService;
    private @Nullable RemoteApiServer remoteApiServer;
    private @Nullable BotEventDispatcher botEventDispatcher;
    private @Nullable BotMetrics botMetrics;
    private @Nullable ConfigurateRuntimeSettingsLoader runtimeSettingsLoader;
    private @Nullable CombatProfileLoader combatProfileLoader;
    private RuntimeSettings runtimeSettings = RuntimeSettings.defaults();
    private @Nullable CombatProfileCatalog combatProfileCatalog;
    private @Nullable CoreExtensionRegistry extensionRegistry;
    private @Nullable CoreAddonRegistry addonRegistry;
    private @Nullable UltimateBotAddonEngine addonEngine;
    private static @Nullable UltimateBot instance;

    @Override
    public void onEnable() {
        instance = this;
        this.wrapperManager = new WrapperManager(this);
        com.monkey.ultimatebot.logging.BootLogger boot = new com.monkey.ultimatebot.logging.BootLogger(this);

        try {
            new BootstrapCoordinator(boot).run();

        } catch (Throwable error) {
            boot.fail(error);
            new ShutdownCoordinator().run();
            throw error;
        }
    }

    @Override
    public void onDisable() {
        new ShutdownCoordinator().run();
        instance = null;
    }

    public @Nullable ITrainingBot getBot(Player player) {
        if (botManager == null || !botManager.isBotSpawned(player.getUniqueId())) {
            return null;
        }
        return botManager.getBotSafe(player.getUniqueId());
    }

    public BotRegistry getBotRegistry() {
        return Objects.requireNonNull(botRegistry, "botRegistry is not initialized");
    }

    public @Nullable BotRegistry getBotRegistryOrNull() {
        return botRegistry;
    }

    public BotManager getBotManager() {
        return Objects.requireNonNull(botManager, "botManager is not initialized");
    }

    public TargetingService getTargetingService() {
        return Objects.requireNonNull(targetingService, "targetingService is not initialized");
    }

    public RuntimeSettings getRuntimeSettings() {
        return runtimeSettings;
    }

    public CombatProfileCatalog getCombatProfileCatalog() {
        return Objects.requireNonNull(combatProfileCatalog, "combatProfileCatalog is not initialized");
    }

    public PlayerOptions getPlayerOptions() {
        return Objects.requireNonNull(playerOptions, "playerOptions is not initialized");
    }

    public static UltimateBot getInstance() {
        return Objects.requireNonNull(instance, "UltimateBot is not enabled");
    }

    public BotEventDispatcher getBotEventDispatcher() {
        return Objects.requireNonNull(botEventDispatcher, "botEventDispatcher is not initialized");
    }

    public BotMetrics getBotMetrics() {
        return Objects.requireNonNull(botMetrics, "botMetrics is not initialized");
    }

    public WrapperManager getWrapperManager() {
        return Objects.requireNonNull(wrapperManager, "wrapperManager is not initialized");
    }

    public @Nullable WrapperManager getWrapperManagerOrNull() {
        return wrapperManager;
    }

    public WorldGuardPvpService getWorldGuardPvpService() {
        return Objects.requireNonNull(worldGuardPvpService, "worldGuardPvpService is not initialized");
    }

    public WorldProtectionService getWorldProtectionService() {
        return Objects.requireNonNull(worldProtectionService, "worldProtectionService is not initialized");
    }

    public CoreExtensionRegistry getExtensionRegistry() {
        return Objects.requireNonNull(extensionRegistry, "extensionRegistry is not initialized");
    }

    public void markCompatibilityBot(Entity entity) {
        if (guardAddonManager != null) {
            guardAddonManager.markBot(entity);
        }
    }

    public void forgetCompatibilityBot(UUID entityUUID) {
        if (guardAddonManager != null) {
            guardAddonManager.forgetBot(entityUUID);
        }
    }

    public void reloadPluginConfiguration() {
        reloadConfig();
        if (runtimeSettingsLoader == null) {
            runtimeSettingsLoader = new ConfigurateRuntimeSettingsLoader(
                    getDataFolder().toPath().resolve("config.yml"), getLogger());
        }
        runtimeSettings = runtimeSettingsLoader.load();
        if (worldProtectionService != null) {
            worldProtectionService.reconfigure(runtimeSettings.worldProtection());
        }
        if (combatProfileLoader == null) {
            combatProfileLoader =
                    new CombatProfileLoader(getDataFolder().toPath().resolve("combat-modes.yml"), getLogger());
        }
        combatProfileCatalog = combatProfileLoader.load();
        combatProfileCatalog.bindPlatformCapabilities(NMSBridgeManager.capabilities());
        if (targetingService != null) {
            targetingService.reconfigure(runtimeSettings.targetCache());
        }
        reloadLanguageConfiguration();
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
            return getConfig().getString(path, path);
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

    private void registerListener(List<String> registeredListeners, String label, Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
        registeredListeners.add(label);
    }


    private boolean isCombatLogXListenerAvailable() {
        return getServer().getPluginManager().getPlugin("CombatLogX") != null
                && hasRuntimeClass("com.github.sirblobman.combatlogx.api.event.PlayerPreTagEvent");
    }

    private BukkitLampConfig<BukkitCommandActor> lampConfigForPlatform() {
        BukkitLampConfig<BukkitCommandActor> config = BukkitLampConfig.createDefault(this);
        if (hasRuntimeClass("org.bukkit.event.server.ServerLoadEvent")) {
            return config;
        }
        getLogger().info("[UltimateBot] Lamp brigadier disabled (ServerLoadEvent unavailable)");
        try {
            Field field = BukkitLampConfig.class.getDeclaredField("disableBrigadier");
            field.setAccessible(true);
            field.setBoolean(config, true);
        } catch (ReflectiveOperationException error) {
            getLogger().warning("Failed to disable Lamp brigadier for legacy Paper: " + error);
        }
        return config;
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
        if (message == null || message.trim().isEmpty()) {
            return error.getClass().getSimpleName();
        }
        return error.getClass().getSimpleName() + " -> " + message;
    }

    private void cleanupRuntimeState() {
        if (remoteApiServer != null) {
            remoteApiServer.stop();
            remoteApiServer = null;
        }

        if (guardAddonManager != null) {
            guardAddonManager.close();
            guardAddonManager = null;
        }

        if (updateManager != null) {
            updateManager.shutdown();
            updateManager = null;
        }

        if (licenseManager != null) {
            licenseManager.shutdown();
            licenseManager = null;
        }

        if (placeholderCoordinator != null) {
            placeholderCoordinator.unregister();
            placeholderCoordinator = null;
        }
        if (botManager != null) {
            botManager.despawnAll(BotDespawnReason.PLUGIN_DISABLE);
            botManager = null;
        }
        if (addonEngine != null) {
            addonEngine.close();
            addonEngine = null;
        }
        if (extensionRegistry != null) {
            extensionRegistry.close();
            extensionRegistry = null;
        }
        if (addonRegistry != null) {
            addonRegistry.clear();
            addonRegistry = null;
        }
        UltimateBotAPI.unregister();
        if (worldProtectionService != null) {
            worldProtectionService.close();
            worldProtectionService = null;
        }
        if (botRegistry != null) {
            botRegistry.clear();
            botRegistry = null;
        }
        if (botEventDispatcher != null) {
            botEventDispatcher.clear();
            botEventDispatcher = null;
        }
        if (botMetrics != null) {
            botMetrics.close();
            botMetrics = null;
        }
        if (playerOptions != null) {
            playerOptions.clear();
            playerOptions = null;
        }
        targetingService = null;
        worldGuardPvpService = null;
        languageManager = null;
        wrapperManager = null;
        runtimeSettingsLoader = null;
        combatProfileLoader = null;
        combatProfileCatalog = null;
    }

    private final class BootstrapCoordinator {
        private final com.monkey.ultimatebot.logging.BootLogger boot;

        private BootstrapCoordinator(com.monkey.ultimatebot.logging.BootLogger boot) {
            this.boot = boot;
        }

        private void run() {
            configureFilesAndLanguage();
            validateLicense();
            checkUpdates();
            initializeNmsCompatibility();
            initializeRuntimeCore();
            UltimateBotAPI api = wireApi();
            registerCommandsAndListeners(api);
            initializePlaceholders();
            finalizeStartup(api);
            boot.complete();
        }

        private void configureFilesAndLanguage() {
            boot.beginPhase(1, "Config", "Configuration");
            saveDefaultConfig();
            if (!getDataFolder().toPath().resolve("combat-modes.yml").toFile().isFile()) {
                saveResource("combat-modes.yml", false);
            }
            runtimeSettingsLoader = new ConfigurateRuntimeSettingsLoader(
                    getDataFolder().toPath().resolve("config.yml"), getLogger());
            runtimeSettings = runtimeSettingsLoader.load();
            combatProfileLoader = new CombatProfileLoader(getDataFolder().toPath().resolve("combat-modes.yml"), getLogger());
            combatProfileCatalog = combatProfileLoader.load();
            reloadLanguageConfiguration();

            com.monkey.ultimatebot.config.ConfigLoadReport report = runtimeSettingsLoader.lastReport();
            boot.config("Keys read: " + report.keysRead() + " | invalid: " + report.invalidCount());
            for (com.monkey.ultimatebot.config.ConfigLoadReport.InvalidKey key : report.invalidKeys()) {
                boot.config("\"" + key.path() + "\" = " + key.invalidValue() + " -> default " + key.defaultValue());
            }
            boot.config("Language -> "
                    + Objects.requireNonNull(languageManager, "languageManager is not initialized")
                            .getActiveLanguageFileName());
            boot.completePhase("config ready");
        }

        private void validateLicense() {
            boot.beginPhase(2, "Boot", "License Validation");
            licenseManager = new LicenseManager(UltimateBot.this);
            LicenseStartupResult licenseResult = licenseManager.validateOnStartup();
            if (!licenseResult.allowed()) {
                boot.warn("License " + licenseResult.reasonCode() + " -> " + licenseResult.message());
                throw new IllegalStateException("License validation failed: " + licenseResult.reasonCode());
            }
            if (licenseResult.graceMode()) {
                boot.warn("License -> " + licenseResult.message());
            } else {
                boot.boot("License -> " + licenseResult.message());
            }
            boot.completePhase("license ready");
        }

        private void checkUpdates() {
            boot.beginPhase(3, "Boot", "Update Check");
            updateManager = new UpdateManager(UltimateBot.this);
            UpdateStartupResult updateResult = updateManager.checkOnStartup();
            if (updateResult.checkFailed() || updateResult.updateAvailable()) {
                boot.warn("Update -> " + updateResult.message());
            } else {
                boot.boot("Update -> " + updateResult.message());
            }
            boot.completePhase("update check completed");
        }

        private void initializeNmsCompatibility() {
            boot.beginPhase(4, "NMS", "Compatibility");
            NMSBridgeManager.init(getLogger());
            Objects.requireNonNull(combatProfileCatalog, "combatProfileCatalog is not initialized")
                    .bindPlatformCapabilities(NMSBridgeManager.capabilities());

            new com.monkey.ultimatebot.logging.NmsBootScanner(boot).scan();

            if (!NMSBridgeManager.isBotRuntimeSupported()) {
                boot.warn("NMS -> Limited mode: fake-player bots not implemented for this revision");
            }
            List<CombatMode> platformDisabled = combatProfileCatalog.platformDisabledModes();
            if (!platformDisabled.isEmpty()) {
                boot.nms("Platform-disabled modes -> "
                        + platformDisabled.stream().map(CombatMode::name).collect(Collectors.joining(", ")));
            }
            boot.completePhase("server compatibility resolved");
        }

        private void initializeRuntimeCore() {
            boot.beginPhase(5, "Module", "Runtime Services");
            long t0 = System.currentTimeMillis();

            targetingService = new TargetingService(runtimeSettings.targetCache());
            boot.module("TargetingService", System.currentTimeMillis() - t0);

            t0 = System.currentTimeMillis();
            playerOptions = new PlayerOptions();
            botRegistry = new BotRegistry();
            extensionRegistry = new CoreExtensionRegistry();
            addonRegistry = new CoreAddonRegistry();
            boot.module("BotRegistry", System.currentTimeMillis() - t0);

            t0 = System.currentTimeMillis();
            botMetrics = new BotMetrics(
                    getConfig().getBoolean("addons.metrics.enabled", false),
                    getConfig().getBoolean("addons.metrics.prometheus-endpoint-enabled", false),
                    PluginMetaAccess.version(UltimateBot.this),
                    com.monkey.ultimatebot.access.runtime.MinecraftVersionAccess.minecraftVersion(),
                    botRegistry,
                    targetingService,
                    getDataFolder().toPath(),
                    getClassLoader(),
                    getLogger());
            boot.module("BotMetrics", System.currentTimeMillis() - t0);

            t0 = System.currentTimeMillis();
            botEventDispatcher = new BotEventDispatcher(UltimateBot.this, botMetrics);
            worldGuardPvpService = new WorldGuardPvpService(UltimateBot.this);
            worldProtectionService = new WorldProtectionService(UltimateBot.this, runtimeSettings.worldProtection());
            botManager = new BotManager(UltimateBot.this);
            boot.module("BotManager", System.currentTimeMillis() - t0);

            if (worldGuardPvpService.isAvailable()) {
                boot.boot("WorldGuard -> found, PvP region check enabled");
            } else {
                boot.warn("WorldGuard -> not found, PvP region check disabled");
            }

            boot.completePhase("runtime core created");
        }

        private UltimateBotAPI wireApi() {
            boot.beginPhase(6, "Boot", "API Wiring");
            BotManager manager = Objects.requireNonNull(botManager, "botManager is not initialized");
            BotRegistry registry = Objects.requireNonNull(botRegistry, "botRegistry is not initialized");
            PlayerOptions options = Objects.requireNonNull(playerOptions, "playerOptions is not initialized");
            CoreExtensionRegistry extensions =
                    Objects.requireNonNull(extensionRegistry, "extensionRegistry is not initialized");
            CoreAddonRegistry addons = Objects.requireNonNull(addonRegistry, "addonRegistry is not initialized");
            UltimateBotAPI api = new UltimateBotAPI(
                    UltimateBot.this,
                    new CoreBotManagerAdapter(UltimateBot.this, manager, registry, options),
                    new CoreBotRegistryAdapter(registry),
                    extensions,
                    addons);
            boot.boot("API -> adapters wired");
            boot.completePhase("public API prepared");
            return api;
        }

        private void registerCommandsAndListeners(UltimateBotAPI api) {
            boot.beginPhase(7, "Module", "Commands and Listeners");
            List<String> registeredCommands = Collections.unmodifiableList(java.util.Arrays.asList(
                    "bot", "botevent", "botally", "botteamally", "ultimatebotreload"));
            BukkitLampConfig<BukkitCommandActor> lampConfig = lampConfigForPlatform();
            BukkitLamp.builder(lampConfig)
                    .exceptionHandler(new UltimateBotExceptionHandler(UltimateBot.this))
                    .build()
                    .register(
                            new BotCommand(UltimateBot.this),
                            new BotEventCommand(UltimateBot.this),
                            new BotAllyCommand(UltimateBot.this),
                            new BotTeamAllyCommand(UltimateBot.this),
                            new ReloadCommand(UltimateBot.this));

            List<String> registeredListeners = new ArrayList<>();
            List<String> disabledListeners = new ArrayList<>();
            CoreExtensionRegistry extensions =
                    Objects.requireNonNull(extensionRegistry, "extensionRegistry is not initialized");
            CoreAddonRegistry addons = Objects.requireNonNull(addonRegistry, "addonRegistry is not initialized");
            BotRegistry registry = Objects.requireNonNull(botRegistry, "botRegistry is not initialized");
            WorldProtectionService protectionService =
                    Objects.requireNonNull(worldProtectionService, "worldProtectionService is not initialized");

            long t0 = System.currentTimeMillis();
            addonEngine = new UltimateBotAddonEngine(UltimateBot.this, api, extensions, addons);
            int externalAddons = addonEngine.loadAll();
            boot.module("AddonEngine", System.currentTimeMillis() - t0);
            if (externalAddons > 0) {
                boot.boot("External addons -> " + externalAddons + " loaded");
            }

            registerListener(
                    registeredListeners, "extension lifecycle", new ExtensionOwnerListener(UltimateBot.this, extensions));
            if (getConfig().getBoolean("addons.guard.enabled", true)) {
                guardAddonManager = new GuardAddonManager(UltimateBot.this, registry);
                if (guardAddonManager.load()) {
                    registeredListeners.add("guard addon");
                } else {
                    disabledListeners.add("guard addon -> unavailable");
                }
            } else {
                disabledListeners.add("guard addon -> disabled in config");
            }
            registerListener(registeredListeners, "bot explosion events", new BotExplosionListener());
            registerListener(
                    registeredListeners, "world protection", new WorldProtectionListener(protectionService));
            registerListener(registeredListeners, "bot runtime events", new BotRuntimeEventListener(UltimateBot.this));
            registerListener(registeredListeners, "required", new PlayerCheckListener(UltimateBot.this));

            boolean combatLogXAvailable = isCombatLogXListenerAvailable();
            if (combatLogXAvailable) {
                try {
                    registerListener(registeredListeners, "optional integration",
                            new PlayerTagListener(UltimateBot.this));
                    boot.boot("CombatLogX -> found, tag listener registered");
                } catch (Throwable error) {
                    disabledListeners.add("optional integration -> " + formatListenerError(error));
                    boot.warn("CombatLogX -> registration failed: " + formatListenerError(error));
                }
            } else {
                disabledListeners.add("optional integration -> CombatLogX dependency unavailable");
                boot.warn("CombatLogX -> not found, tag listener disabled");
            }

            boot.boot("Commands -> " + registeredCommands.size()
                    + " | Listeners -> active=" + registeredListeners.size()
                    + " disabled=" + disabledListeners.size());
            boot.completePhase("hooks registered");
        }

        private void initializePlaceholders() {
            boot.beginPhase(8, "Boot", "Placeholder Integration");
            if (PlaceholderApiSupport.isAvailable()) {
                placeholderCoordinator = PlaceholderApiSupport.createRegistration(UltimateBot.this);
                if (placeholderCoordinator != null) {
                    boolean registered = placeholderCoordinator.register();
                    List<String> keys = placeholderCoordinator.getRegisteredPlaceholderKeys();
                    if (registered) {
                        boot.boot("PlaceholderAPI -> found, " + keys.size() + " placeholders registered");
                    } else {
                        boot.warn("PlaceholderAPI -> found, hook registration failed");
                    }
                } else {
                    boot.warn("PlaceholderAPI -> found, hook initialization failed");
                }
            } else {
                boot.warn("PlaceholderAPI -> not found, placeholders disabled");
            }
            boot.completePhase("placeholder phase completed");
        }

        private void finalizeStartup(UltimateBotAPI api) {
            boot.beginPhase(9, "Boot", "Finalize");
            UltimateBotAPI.register(api);
            getServer().getPluginManager().callEvent(new UltimateBotReadyEvent(api));
            boot.boot("UltimateBotReadyEvent fired");
            remoteApiServer = new RemoteApiServer(UltimateBot.this, api);
            remoteApiServer.start();

            try {
                Metrics metrics = new Metrics(UltimateBot.this, BSTATS_PLUGIN_ID);
                metrics.addCustomChart(new SimplePie("plugin_version", () -> PluginMetaAccess.version(UltimateBot.this)));
                boot.boot("bStats -> metrics enabled");
            } catch (Throwable metricsError) {
                boot.warn("bStats -> metrics init failed: " + formatListenerError(metricsError));
            }

            if (licenseManager != null) {
                licenseManager.startHeartbeat();
            }
            if (updateManager != null) {
                updateManager.startRuntime();
            }
            boot.completePhase("enable sequence completed");
        }
    }

    private final class ShutdownCoordinator {
        private void run() {
            cleanupRuntimeState();
        }
    }

    private void applyRuntimeBotConfiguration() {
        if (botRegistry == null) {
            return;
        }

        boolean infiniteResources = getConfig().getBoolean("bot.combat.infinite-resources", true);
        for (ITrainingBot bot : botRegistry.getAllBots().values()) {
            if (bot != null && bot.getBotAI() != null) {
                bot.getBotAI().getInventoryController().setInfiniteResources(infiniteResources);
                bot.getBotAI().getMovementController().reconfigureBlockStateCache(runtimeSettings.blockStateCache());
            }
        }
    }

    private String joinOrNone(Iterable<String> values) {
        if (values == null) {
            return "none";
        }
        String joined = String.join(", ", values);
        return joined.trim().isEmpty() ? "none" : joined;
    }
}
