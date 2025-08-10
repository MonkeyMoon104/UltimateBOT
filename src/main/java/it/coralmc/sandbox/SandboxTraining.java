package it.coralmc.sandbox;

import it.coralmc.sandbox.bot.BotRegistry;
import it.coralmc.sandbox.bot.BotSpawner;
import it.coralmc.sandbox.commands.BotCommand;
import it.coralmc.sandbox.commands.ReloadCommand;
import it.coralmc.sandbox.listener.PlayerQuitListener;
import it.coralmc.sandbox.utils.armor.PlayerOptions;
import org.bukkit.plugin.java.JavaPlugin;

public final class SandboxTraining extends JavaPlugin {

    private PlayerOptions playerOptions;
    private BotRegistry botRegistry;
    private BotSpawner botSpawner;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.playerOptions = new PlayerOptions();
        this.botRegistry = new BotRegistry();
        this.botSpawner = new BotSpawner(this);

        getCommand("bot").setExecutor(new BotCommand(this));
        getCommand("sbreload").setExecutor(new ReloadCommand(this));
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
    }

    @Override
    public void onDisable() {
        botSpawner.despawnAllBots();
        playerOptions.clear();
    }

    public BotRegistry getBotRegistry() {
        return botRegistry;
    }

    public BotSpawner getBotSpawner() {
        return botSpawner;
    }

    public PlayerOptions getPlayerOptions() {
        return playerOptions;
    }
}
