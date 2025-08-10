package it.coralmc.sandbox.listener;

import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.bot.BotSpawner;
import it.coralmc.sandbox.utils.ChatColorUtils;
import it.coralmc.sandbox.utils.armor.PlayerOptions;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final SandboxTraining plugin;
    private final BotSpawner botSpawner;
    private final PlayerOptions playerOptions;

    public PlayerQuitListener(SandboxTraining plugin) {
        this.plugin = plugin;
        this.botSpawner = plugin.getBotSpawner();
        this.playerOptions = plugin.getPlayerOptions();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (botSpawner.isBotSpawned(player.getUniqueId())) {
            botSpawner.despawnBot(player);
        }

        playerOptions.remove(player.getUniqueId());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        if (botSpawner.isBotSpawned(player.getUniqueId())) {
            botSpawner.despawnBotInWorld(player, event.getFrom());
            player.closeInventory();

            String despawnMsg = plugin.getConfig().getString("messages.despawn-bot", "&cBot despawned!");
            player.sendMessage(ChatColorUtils.translate(despawnMsg));

            playerOptions.remove(player.getUniqueId());
        }
    }
}
