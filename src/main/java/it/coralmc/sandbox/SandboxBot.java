package it.coralmc.sandbox;

import it.coralmc.sandbox.bot.util.TrainingBot;
import it.coralmc.sandbox.commands.BotCommand;
import it.coralmc.sandbox.listener.InventoryClickListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public final class SandboxBot extends JavaPlugin {

    private static SandboxBot instance;
    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        getCommand("bot").setExecutor(new BotCommand());
        getServer().getPluginManager().registerEvents(new InventoryClickListener(), this);
    }

    @Override
    public void onDisable() {
        instance = null;
    }

    public static SandboxBot getInstance() {
        return instance;
    }
}