package it.coralmc.sandbox.listener;

import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.bot.BotManager;
import it.coralmc.sandbox.bot.BotSpawn;
import it.coralmc.sandbox.utils.ChatColorUtils;
import it.coralmc.sandbox.utils.armor.PlayerOptions;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerCheckListener implements Listener {

    private final SandboxTraining plugin;
    private final BotManager botManager;
    private final PlayerOptions playerOptions;

    public PlayerCheckListener(SandboxTraining plugin) {
        this.plugin = plugin;
        this.botManager = plugin.getBotManager();
        this.playerOptions = plugin.getPlayerOptions();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (botManager.isBotSpawned(player.getUniqueId())) {
            botManager.despawnBot(player);
        }

        playerOptions.remove(player.getUniqueId());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        if (botManager.isBotSpawned(player.getUniqueId())) {
            botManager.despawnBotInWorld(player, event.getFrom());
            player.closeInventory();

            String despawnMsg = plugin.getConfig().getString("messages.despawn-bot", "&cBot despawned!");
            player.sendMessage(ChatColorUtils.translate(despawnMsg));

            playerOptions.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onDead(PlayerDeathEvent event) {
        Player player = event.getPlayer();

        if (botManager.isBotSpawned(player.getUniqueId())) {
            botManager.despawnBot(player);

            String despawnMsg = plugin.getConfig().getString("messages.despawn-bot", "&cBot despawned!");
            player.sendMessage(ChatColorUtils.translate(despawnMsg));

            playerOptions.remove(player.getUniqueId());
        }
    }
}
