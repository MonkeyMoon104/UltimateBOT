package com.monkey.mcbot;

import com.monkey.mcbot.bot.BotManager;
import com.monkey.mcbot.bot.BotRegistry;
import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.bot.ai.services.TargetingService;
import com.monkey.mcbot.commands.BotAllyCommand;
import com.monkey.mcbot.commands.BotCommand;
import com.monkey.mcbot.commands.BotEventCommand;
import com.monkey.mcbot.commands.ReloadCommand;
import com.monkey.mcbot.listener.PlayerCheckListener;
import com.monkey.mcbot.listener.PlayerTagListener;
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
        saveDefaultConfig();

        NMSBridgeManager.init();

        this.targetingService = new TargetingService();
        this.playerOptions = new PlayerOptions();
        this.botRegistry = new BotRegistry();
        this.botManager = new BotManager(this);

        getCommand("bot").setExecutor(new BotCommand(this));
        getCommand("botevent").setExecutor(new BotEventCommand(this));
        getCommand("botally").setExecutor(new BotAllyCommand(this));
        getCommand("sbreload").setExecutor(new ReloadCommand(this));
        getServer().getPluginManager().registerEvents(new PlayerCheckListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerTagListener(this), this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            this.placeholderCoordinator = new BotPlaceholderCoordinator(this);
            if (placeholderCoordinator.register()) {
                getLogger().info("Placeholder registrati con successo!");
            } else {
                getLogger().warning("Errore nella registrazione dei placeholder!");
            }
        } else {
            getLogger().warning("PlaceholderAPI non trovato! I placeholder non saranno disponibili.");
        }
    }

    @Override
    public void onDisable() {
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
