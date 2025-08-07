package it.coralmc.sandbox.listener;

import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.bot.BotSpawner;
import it.coralmc.sandbox.utils.armor.PlayerArmorManager;
import it.coralmc.sandbox.utils.chatcolor.ChatColorUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();

		if (BotSpawner.isBotSpawned(player.getUniqueId())) {
			BotSpawner.despawnBot(player);
		}
	}

	@EventHandler
	public void onWorldChange(PlayerChangedWorldEvent event) {
		Player player = event.getPlayer();

		if (BotSpawner.isBotSpawned(player.getUniqueId())) {
			BotSpawner.despawnBot(player);
			player.closeInventory();

			String despawnMsg = SandboxTraining.getInstance().getConfig().getString("messages.despawn-bot", "&cBot despawned!");
			player.sendMessage(ChatColorUtils.translate(despawnMsg));

			PlayerArmorManager.removePlayerSettings(player.getUniqueId());
		}
	}
}
