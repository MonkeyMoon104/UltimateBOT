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
import com.monkey.mcbot.license.LicenseManager;
import com.monkey.mcbot.license.LicenseStartupResult;
import com.monkey.mcbot.listener.PlayerCheckListener;
import com.monkey.mcbot.listener.PlayerTagListener;
import com.monkey.mcbot.logging.MinecraftBotLogging;
import com.monkey.mcbot.nms.NMSBridgeManager;
import com.monkey.mcbot.placeholders.BotPlaceholderCoordinator;
import com.monkey.mcbot.utils.armor.PlayerOptions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class MinecraftBot extends JavaPlugin {

    private PlayerOptions playerOptions;
    private BotRegistry botRegistry;
    private BotManager botManager;
    private BotPlaceholderCoordinator placeholderCoordinator;
    private TargetingService targetingService;
    private LicenseManager licenseManager;
    private static MinecraftBot instance;

    @Override
    public void onEnable() {
        instance = this;
        MinecraftBotLogging.StartupSession startup = MinecraftBotLogging.beginBootstrap(this);

        try {
            startup.beginPhase(1, "Boot", "Configuration");
            saveDefaultConfig();
            startup.ready("Config", "default file verified");
            startup.detail("Path", getDataFolder().toPath().resolve("config.yml").toString());
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

            startup.beginPhase(3, "NMS", "Compatibility");
            NMSBridgeManager.init(getLogger());
            startup.markNmsBridge(NMSBridgeManager.get().getClass().getSimpleName(), NMSBridgeManager.getSupportedVersions());
            startup.detail("Catalog", MinecraftBotLogging.catalogSummary());
            startup.detail("Controllers", MinecraftBotLogging.controllerSummary());
            startup.completePhase("server compatibility resolved");

            startup.beginPhase(4, "Core", "Runtime");
            this.targetingService = new TargetingService();
            this.playerOptions = new PlayerOptions();
            this.botRegistry = new BotRegistry();
            this.botManager = new BotManager(this);
            startup.ready("Runtime", "services created");
            startup.detail("Services", "TargetingService, PlayerOptions, BotRegistry, BotManager");
            startup.detail("Caches", "bots=" + botRegistry.size() + " | playerOptions=" + playerOptions.size());
            startup.completePhase("runtime core created");

            startup.beginPhase(5, "API", "Wiring");
            MinecraftBotAPI api = new MinecraftBotAPI(
                    this,
                    new CoreBotManagerAdapter(this, botManager, botRegistry, playerOptions),
                    new CoreBotRegistryAdapter(botRegistry)
            );
            startup.ready("API", "adapters wired");
            startup.detail("Manager", api.getBotManager().getClass().getSimpleName());
            startup.detail("Registry", api.getBotRegistry().getClass().getSimpleName());
            startup.completePhase("public API prepared");

            startup.beginPhase(6, "Hooks", "Commands and Listeners");

            List<String> registeredCommands = new ArrayList<>();
            registerCommand(startup, registeredCommands, "bot", new BotCommand(this));
            registerCommand(startup, registeredCommands, "botevent", new BotEventCommand(this));
            registerCommand(startup, registeredCommands, "botally", new BotAllyCommand(this));

            BotTeamAllyCommand botTeamAllyCommand = new BotTeamAllyCommand(this);
            registerCommand(startup, registeredCommands, "botteamally", botTeamAllyCommand, botTeamAllyCommand);
            registerCommand(startup, registeredCommands, "mcbreload", new ReloadCommand(this));
            startup.markCommands(registeredCommands);

            List<String> registeredListeners = new ArrayList<>();
            registerListener(startup, registeredListeners, new PlayerCheckListener(this));
            registerListener(startup, registeredListeners, new PlayerTagListener(this));
            startup.markListeners(registeredListeners);
            startup.ready("Hooks", "registrations completed");
            startup.detail("Commands", joinOrNone(registeredCommands));
            startup.detail("Listeners", joinOrNone(registeredListeners));
            startup.completePhase("hooks registered");

            startup.beginPhase(7, "PAPI", "Placeholder Integration");
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                this.placeholderCoordinator = new BotPlaceholderCoordinator(this);
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
                startup.markPlaceholders(false, false, List.of());
                startup.warn("PlaceholderAPI", "not found");
            }
            startup.completePhase("placeholder phase completed");

            startup.beginPhase(8, "Boot", "Finalize");
            MinecraftBotAPI.register(api);
            startup.markApiPublished();
            MinecraftBotLogging.logApiRegistered(getLogger(), api);
            getServer().getPluginManager().callEvent(new MinecraftBotReadyEvent(api));
            startup.ready("Event", MinecraftBotReadyEvent.class.getSimpleName() + " fired");
            if (licenseManager != null) {
                licenseManager.startHeartbeat();
                startup.ready("License", "heartbeat started");
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
                                  Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
        registeredListeners.add(listener.getClass().getSimpleName());
    }

    private void cleanupRuntimeState() {
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
    }

    private String joinOrNone(Iterable<String> values) {
        if (values == null) {
            return "none";
        }
        String joined = String.join(", ", values);
        return joined.isBlank() ? "none" : joined;
    }
}
