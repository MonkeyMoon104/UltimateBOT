package it.coralmc.sandbox;

import it.coralmc.sandbox.bot.BotManager;
import it.coralmc.sandbox.bot.BotRegistry;
import it.coralmc.sandbox.bot.ai.TrainingBot;
import it.coralmc.sandbox.commands.BotCommand;
import it.coralmc.sandbox.commands.ReloadCommand;
import it.coralmc.sandbox.listener.PlayerCheckListener;
import it.coralmc.sandbox.placeholders.BotPlaceholderCoordinator;
import it.coralmc.sandbox.utils.armor.PlayerOptions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class SandboxTraining extends JavaPlugin {

    private PlayerOptions playerOptions;
    private BotRegistry botRegistry;
    private BotManager botManager;
    private BotPlaceholderCoordinator placeholderCoordinator;
    private static SandboxTraining instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.playerOptions = new PlayerOptions();
        this.botRegistry = new BotRegistry();
        this.botManager = new BotManager(this);

        getCommand("bot").setExecutor(new BotCommand(this));
        getCommand("sbreload").setExecutor(new ReloadCommand(this));
        getServer().getPluginManager().registerEvents(new PlayerCheckListener(this), this);

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

    public TrainingBot getBot(Player player) {
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

    public PlayerOptions getPlayerOptions() {
        return playerOptions;
    }

    public static SandboxTraining getInstance() {
        return instance;
    }
}