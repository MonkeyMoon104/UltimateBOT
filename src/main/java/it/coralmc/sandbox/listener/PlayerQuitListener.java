package it.coralmc.sandbox.listener;

import it.coralmc.sandbox.bot.BotSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (BotSpawner.isBotSpawned(player.getUniqueId())) {
            BotSpawner.despawnBot(player);
        }
    }
}
