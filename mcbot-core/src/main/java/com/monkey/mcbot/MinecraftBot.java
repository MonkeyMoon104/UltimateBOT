package com.monkey.mcbot;

import com.monkey.mcbot.api.MinecraftBotAPI;
import com.monkey.mcbot.api.event.MinecraftBotReadyEvent;
import com.monkey.mcbot.bot.BotManager;
import com.monkey.mcbot.bot.BotRegistry;
import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.bot.ai.services.TargetingService;
import com.monkey.mcbot.commands.BotAllyCommand;
import com.monkey.mcbot.commands.BotCommand;
import com.monkey.mcbot.commands.BotEventCommand;
import com.monkey.mcbot.commands.BotTeamAllyCommand;
import com.monkey.mcbot.commands.ReloadCommand;
import com.monkey.mcbot.integration.api.CoreBotManagerAdapter;
import com.monkey.mcbot.integration.api.CoreBotRegistryAdapter;
import com.monkey.mcbot.listener.PlayerCheckListener;
import com.monkey.mcbot.listener.PlayerTagListener;
import com.monkey.mcbot.logging.MinecraftBotLogging;
import com.monkey.mcbot.nms.NMSBridgeManager;
import com.monkey.mcbot.placeholders.BotPlaceholderCoordinator;
import com.monkey.mcbot.utils.armor.PlayerOptions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class MinecraftBot extends JavaPlugin {

    private PlayerOptions playerOptions;
    private BotRegistry botRegistry;
    private BotManager botManager;
    private BotPlaceholderCoordinator placeholderCoordinator;
    private TargetingService targetingService;
    private static MinecraftBot instance;

    @Override
    public void onEnable() {
        instance = this;
        MinecraftBotLogging.logBootstrapStart(this);
        boolean placeholderPresent = false;
        boolean placeholderRegistered = false;

        MinecraftBotLogging.logStartupPhase(getLogger(), 1, "Configuration");
        saveDefaultConfig();
        MinecraftBotLogging.logComponentReady(
                getLogger(),
                "Config",
                "default config loaded | keys=" + getConfig().getKeys(true).size()
        );

        MinecraftBotLogging.logStartupPhase(getLogger(), 2, "NMS Bridge");
        NMSBridgeManager.init();
        MinecraftBotLogging.logComponentReady(getLogger(), "NMS", NMSBridgeManager.get().getClass().getSimpleName());
        MinecraftBotLogging.logBotTypeCatalog(getLogger());
        MinecraftBotLogging.logControllerCatalog(getLogger());

        MinecraftBotLogging.logStartupPhase(getLogger(), 3, "Core Services");
        this.targetingService = new TargetingService();
        MinecraftBotLogging.logComponentReady(getLogger(), "TargetingService", targetingService.getClass().getSimpleName());
        this.playerOptions = new PlayerOptions();
        MinecraftBotLogging.logComponentReady(getLogger(), "PlayerOptions", playerOptions.getClass().getSimpleName());
        this.botRegistry = new BotRegistry();
        MinecraftBotLogging.logComponentReady(getLogger(), "BotRegistry", botRegistry.getClass().getSimpleName());
        this.botManager = new BotManager(this);
        MinecraftBotLogging.logComponentReady(getLogger(), "BotManager", botManager.getClass().getSimpleName());

        MinecraftBotLogging.logStartupPhase(getLogger(), 4, "API Wiring");
        MinecraftBotAPI api = new MinecraftBotAPI(
                this,
                new CoreBotManagerAdapter(this, botManager, botRegistry, playerOptions),
                new CoreBotRegistryAdapter(botRegistry)
        );
        MinecraftBotLogging.logComponentReady(getLogger(), "CoreBotManagerAdapter", "public API adapter initialized");
        MinecraftBotLogging.logComponentReady(getLogger(), "CoreBotRegistryAdapter", "public API registry adapter initialized");

        MinecraftBotLogging.logStartupPhase(getLogger(), 5, "Commands & Listeners");
        getCommand("bot").setExecutor(new BotCommand(this));
        getCommand("botevent").setExecutor(new BotEventCommand(this));
        getCommand("botally").setExecutor(new BotAllyCommand(this));

        BotTeamAllyCommand botTeamAllyCommand = new BotTeamAllyCommand(this);
        getCommand("botteamally").setExecutor(botTeamAllyCommand);
        getCommand("botteamally").setTabCompleter(botTeamAllyCommand);
        MinecraftBotLogging.logComponentReady(getLogger(), "Commands", "bot,botally,botevent,botteamally,sbreload");

        getCommand("sbreload").setExecutor(new ReloadCommand(this));
        getServer().getPluginManager().registerEvents(new PlayerCheckListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerTagListener(this), this);
        MinecraftBotLogging.logComponentReady(getLogger(), "Listeners", "PlayerCheckListener,PlayerTagListener");

        MinecraftBotLogging.logStartupPhase(getLogger(), 6, "Placeholder Integration");
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderPresent = true;
            this.placeholderCoordinator = new BotPlaceholderCoordinator(this);
            if (placeholderCoordinator.register()) {
                getLogger().info("Placeholder registrati con successo!");
                placeholderRegistered = true;
                MinecraftBotLogging.logComponentReady(getLogger(), "PlaceholderAPI", "registered");
            } else {
                getLogger().warning("Errore nella registrazione dei placeholder!");
                MinecraftBotLogging.logComponentReady(getLogger(), "PlaceholderAPI", "present but registration failed");
            }
        } else {
            getLogger().warning("PlaceholderAPI non trovato! I placeholder non saranno disponibili.");
            MinecraftBotLogging.logComponentReady(getLogger(), "PlaceholderAPI", "not present");
        }

        MinecraftBotLogging.logStartupPhase(getLogger(), 7, "API Registration");
        MinecraftBotAPI.register(api);
        MinecraftBotLogging.logApiRegistered(getLogger(), api);
        getServer().getPluginManager().callEvent(new MinecraftBotReadyEvent(api));
        MinecraftBotLogging.logBootstrapCompleted(this, placeholderPresent, placeholderRegistered);
    }

    @Override
    public void onDisable() {
        MinecraftBotAPI.unregister();

        if (placeholderCoordinator != null) {
            placeholderCoordinator.unregister();
        }
        if (botManager != null) {
            botManager.despawnAll();
        }
        if (playerOptions != null) {
            playerOptions.clear();
        }
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
}
