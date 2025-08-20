package it.coralmc.sandbox;

import it.coralmc.sandbox.bot.BotManager;
import it.coralmc.sandbox.bot.BotRegistry;
import it.coralmc.sandbox.commands.BotCommand;
import it.coralmc.sandbox.commands.ReloadCommand;
import it.coralmc.sandbox.listener.PlayerCheckListener;
import it.coralmc.sandbox.utils.armor.PlayerOptions;
import org.bukkit.plugin.java.JavaPlugin;

public final class SandboxTraining extends JavaPlugin {

    private PlayerOptions playerOptions;
    private BotRegistry botRegistry;
    private BotManager botManager;
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
    }

    @Override
    public void onDisable() {
        botManager.despawnAllBots();
        playerOptions.clear();
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
