package it.coralmc.sandbox;

import it.coralmc.sandbox.commands.BotCommand;
import it.coralmc.sandbox.listener.InventoryClickListener;
import it.coralmc.sandbox.listener.PlayerQuitListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class SandboxTraining extends JavaPlugin {

	private static SandboxTraining instance;

	public static SandboxTraining getInstance() {
		return instance;
	}

	@Override
	public void onEnable() {
		instance = this;

		saveDefaultConfig();

		getCommand("bot").setExecutor(new BotCommand());
		getServer().getPluginManager().registerEvents(new InventoryClickListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerQuitListener(), this);
	}

	@Override
	public void onDisable() {
		instance = null;
	}
}