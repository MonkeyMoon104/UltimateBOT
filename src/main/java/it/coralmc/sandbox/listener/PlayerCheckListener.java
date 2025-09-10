package it.coralmc.sandbox.listener;

import it.coralmc.sandbox.SandboxTraining;
import it.coralmc.sandbox.bot.BotManager;
import it.coralmc.sandbox.bot.ai.TrainingBot;
import it.coralmc.sandbox.utils.ChatColorUtils;
import it.coralmc.sandbox.utils.armor.PlayerOptions;
import org.bukkit.entity.Entity;
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
            botManager.despawn(player);
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
        boolean wasBotSpawned = botManager.isBotSpawned(player.getUniqueId());

        if (wasBotSpawned) {
            botManager.despawn(player);
            String despawnMsg = plugin.getConfig().getString("messages.despawn-bot", "&cBot despawned!");
            player.sendMessage(ChatColorUtils.translate(despawnMsg));
            playerOptions.remove(player.getUniqueId());
        }

        Entity killer = event.getEntity().getKiller();

        if (killer instanceof org.bukkit.entity.Player && killer.getClass().getSimpleName().equals("BotCraftPlayer")) {
            String deathMessage = plugin.getConfig().getString("messages.dead-bot-message", player.getName() + " was killed by his Bot");
            event.setDeathMessage(deathMessage.replace("{player}", player.getName()));
            return;
        }

        if (killer instanceof TrainingBot) {
            String deathMessage = plugin.getConfig().getString("messages.dead-bot-message", player.getName() + " was killed by his Bot");
            event.setDeathMessage(deathMessage.replace("{player}", player.getName()));
            return;
        }

        if (wasBotSpawned && (killer == null ||
                (event.getDeathMessage() != null && event.getDeathMessage().contains("[Intentional Game Design]")))) {
            String deathMessage = plugin.getConfig().getString("messages.dead-bot-message", player.getName() + " was killed by his Bot");
            event.setDeathMessage(deathMessage.replace("{player}", player.getName()));
            return;
        }

        if (wasBotSpawned && event.getDeathMessage() != null) {
            TrainingBot bot = botManager.getBot(player.getUniqueId());
            if (bot != null && event.getDeathMessage().contains(bot.getName().getString())) {
                String deathMessage = plugin.getConfig().getString("messages.dead-bot-message", player.getName() + " was killed by his Bot");
                event.setDeathMessage(deathMessage.replace("{player}", player.getName()));
            }
        }
    }
}
