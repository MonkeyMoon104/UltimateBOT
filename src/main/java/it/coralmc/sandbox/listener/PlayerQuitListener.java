package it.coralmc.sandbox.listener;

import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.bot.BotSpawner;
import it.coralmc.sandbox.bot.ai.BotAI;
import it.coralmc.sandbox.utils.armor.PlayerArmorManager;
import it.coralmc.sandbox.utils.chatcolor.ChatColorUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

	private final SandboxTraining plugin;
	private final BotSpawner botSpawner;
	private final ChatColorUtils chatColorUtils;
	private final PlayerArmorManager playerArmorManager;

	public PlayerQuitListener (SandboxTraining plugin) {
		this.plugin = plugin;
        this.botSpawner = plugin.getBotSpawner();
        this.chatColorUtils = plugin.getChatColorUtils();
        this.playerArmorManager = plugin.getPlayerArmorManager();
    }

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();

		if (botSpawner.isBotSpawned(player.getUniqueId())) {
			botSpawner.despawnBot(player);
		}
	}

	@EventHandler
	public void onWorldChange(PlayerChangedWorldEvent event) {
		Player player = event.getPlayer();

		if (botSpawner.isBotSpawned(player.getUniqueId())) {
			botSpawner.despawnBot(player);
			player.closeInventory();

			String despawnMsg = plugin.getConfig().getString("messages.despawn-bot", "&cBot despawned!");
			player.sendMessage(chatColorUtils.translate(despawnMsg));

			playerArmorManager.removePlayerSettings(player.getUniqueId());
		}
	}
}
