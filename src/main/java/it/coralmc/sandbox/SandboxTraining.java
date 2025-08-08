package it.coralmc.sandbox;

import it.coralmc.sandbox.bot.BotSpawner;
import it.coralmc.sandbox.bot.util.entity.BotEntityFinder;
import it.coralmc.sandbox.bot.util.equipment.manager.BotEquipmentManager;
import it.coralmc.sandbox.bot.util.packets.Packet;
import it.coralmc.sandbox.bot.util.registry.BotRegistry;
import it.coralmc.sandbox.commands.bot.BotCommand;
import it.coralmc.sandbox.commands.reload.ReloadCommand;
import it.coralmc.sandbox.gui.builder.GUIItemBuilder;
import it.coralmc.sandbox.gui.builder.armor.ArmorUtils;
import it.coralmc.sandbox.listener.InventoryClickListener;
import it.coralmc.sandbox.listener.PlayerQuitListener;
import it.coralmc.sandbox.utils.armor.PlayerArmorManager;
import it.coralmc.sandbox.utils.chatcolor.ChatColorUtils;
import it.coralmc.sandbox.utils.gui.GUISlotHandler;
import org.bukkit.plugin.java.JavaPlugin;

public final class SandboxTraining extends JavaPlugin {

	private PlayerArmorManager playerArmorManager;
	private BotRegistry botRegistry;
	private BotEquipmentManager botEquipmentManager;
	private Packet packet;
	private BotEntityFinder botEntityFinder;
	private BotSpawner botSpawner;
	private GUISlotHandler guiSlotHandler;
	private ChatColorUtils chatColorUtils;
	private ArmorUtils armorUtils;
	private GUIItemBuilder guiItemBuilder;

	@Override
	public void onEnable() {
		saveDefaultConfig();

		this.playerArmorManager = new PlayerArmorManager();
		this.botRegistry = new BotRegistry();
		this.botEquipmentManager = new BotEquipmentManager(botRegistry);
		this.packet = new Packet();

		this.botEntityFinder = new BotEntityFinder(botRegistry);

		this.botSpawner = new BotSpawner(this, botRegistry, botEquipmentManager, packet, botEntityFinder);
		this.guiSlotHandler = new GUISlotHandler();
		this.chatColorUtils = new ChatColorUtils();
		this.armorUtils = new ArmorUtils(this);
		this.guiItemBuilder = new GUIItemBuilder(this);

		getCommand("bot").setExecutor(new BotCommand(this, botEntityFinder));
		getCommand("reload").setExecutor(new ReloadCommand(this));
		getServer().getPluginManager().registerEvents(new InventoryClickListener(this, botRegistry), this);
		getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
	}

	@Override
	public void onDisable() {
		super.onDisable();

		botSpawner.despawnAllBots();
		playerArmorManager.clearAll();
	}

	public BotSpawner getBotSpawner() {
		return botSpawner;
	}

	public GUISlotHandler getGuiSlotHandler() {
		return guiSlotHandler;
	}
	public ChatColorUtils getChatColorUtils() {
		return chatColorUtils;
	}
	public PlayerArmorManager getPlayerArmorManager() {
		return playerArmorManager;
	}

	public ArmorUtils getArmorUtils() {
		return armorUtils;
	}

	public GUIItemBuilder getGuiItemBuilder() {
		return guiItemBuilder;
	}
}
