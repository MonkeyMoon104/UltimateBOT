package com.monkey.mcbot.listener;

import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.bot.BotBroadcaster;
import com.monkey.mcbot.bot.BotManager;
import com.monkey.mcbot.bot.BotOptions;
import com.monkey.mcbot.bot.BotType;
import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.bot.ai.fakeplayer.BotCraftPlayer;
import com.monkey.mcbot.utils.ChatColorUtils;
import com.monkey.mcbot.utils.armor.PlayerOptions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerCheckListener implements Listener {

    private static final LegacyComponentSerializer LEGACY_SECTION_SERIALIZER = LegacyComponentSerializer.legacySection();
    private static final PlainTextComponentSerializer PLAIN_TEXT_SERIALIZER = PlainTextComponentSerializer.plainText();

    private final MinecraftBot plugin;
    private final BotManager botManager;
    private final PlayerOptions playerOptions;

    public PlayerCheckListener(MinecraftBot plugin) {
        this.plugin = plugin;
        this.botManager = plugin.getBotManager();
        this.playerOptions = plugin.getPlayerOptions();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        plugin.getWrapperManager().active().runEntityLater(player, 15L, () -> {
            BotBroadcaster.syncVisibleBotsForPlayer(player, plugin.getBotRegistry().getAllBots().values());
        });
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

            String despawnMsg = plugin.getLangString("messages.despawn-bot", "&cBot despawned!");
            player.sendMessage(ChatColorUtils.translate(despawnMsg));

            playerOptions.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onDead(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        boolean wasBotSpawned = botManager.isBotSpawned(player.getUniqueId());
        String currentDeathMessage = getDeathMessageText(event);

        BotOptions options = null;
        if (wasBotSpawned) {
            ITrainingBot bot = botManager.getBotSafe(player.getUniqueId());
            options = bot != null && bot.getBrainController() != null
                    ? bot.getBrainController().getBotOptions()
                    : null;
            boolean isEventBot = bot != null
                    && bot.getBrainController() != null
                    && options != null
                    && options.getBotType() == BotType.EVENT;

            if (!isEventBot && (options == null || !options.isStayAfterOwnerDeath())) {
                botManager.despawn(player);
                String despawnMsg = plugin.getLangString("messages.despawn-bot", "&cBot despawned!");
                player.sendMessage(ChatColorUtils.translate(despawnMsg));
                playerOptions.remove(player.getUniqueId());
            }
        }

        Entity killer = event.getEntity().getKiller();

        if (killer instanceof BotCraftPlayer) {
            setBotDeathMessage(event, player, options);
            return;
        }

        if (killer instanceof ITrainingBot) {
            setBotDeathMessage(event, player, options);
            return;
        }

        if (wasBotSpawned && (killer == null ||
                (currentDeathMessage != null && currentDeathMessage.contains("[Intentional Game Design]")))) {
            setBotDeathMessage(event, player, options);
            return;
        }

        if (wasBotSpawned && currentDeathMessage != null) {
            ITrainingBot bot = botManager.getBot(player.getUniqueId());
            if (bot != null && currentDeathMessage.contains(bot.asPlayer().getName().getString())) {
                setBotDeathMessage(event, player, options);
            }
        }
    }

    private void setBotDeathMessage(PlayerDeathEvent event, Player player, BotOptions options) {
        if (options != null && !options.isKillMessageEnabled()) {
            event.deathMessage(null);
            return;
        }

        String configured = options == null ? null : options.getCustomKillMessage();
        String deathMessage = configured == null
                ? plugin.getLangString("messages.dead-bot-message", player.getName() + " was killed by his Bot")
                : configured;
        String formatted = ChatColorUtils.translate(deathMessage.replace("{player}", player.getName()));
        event.deathMessage(LEGACY_SECTION_SERIALIZER.deserialize(formatted));
    }

    private String getDeathMessageText(PlayerDeathEvent event) {
        Component deathMessage = event.deathMessage();
        return deathMessage == null ? null : PLAIN_TEXT_SERIALIZER.serialize(deathMessage);
    }
}
