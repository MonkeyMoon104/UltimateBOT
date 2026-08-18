package com.monkey.ultimatebot.listener;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.api.event.combat.BotKillPlayerEvent;
import com.monkey.ultimatebot.api.event.lifecycle.BotDespawnReason;
import com.monkey.ultimatebot.api.model.runtime.BotSnapshot;
import com.monkey.ultimatebot.bot.BotBroadcaster;
import com.monkey.ultimatebot.bot.BotManager;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.BotType;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.integration.api.BotSnapshotMapper;
import com.monkey.ultimatebot.utils.ChatColorUtils;
import com.monkey.ultimatebot.utils.armor.PlayerOptions;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jspecify.annotations.Nullable;

public class PlayerCheckListener implements Listener {

    private final UltimateBot plugin;
    private final BotManager botManager;
    private final PlayerOptions playerOptions;

    private static final class BotKillContext {
        private final UUID ownerUUID;
        private final ITrainingBot bot;
        private final @Nullable BotOptions options;

        private BotKillContext(UUID ownerUUID, ITrainingBot bot, @Nullable BotOptions options) {
            this.ownerUUID = ownerUUID;
            this.bot = bot;
            this.options = options;
        }

        UUID ownerUUID() {
            return ownerUUID;
        }

        ITrainingBot bot() {
            return bot;
        }

        @Nullable BotOptions options() {
            return options;
        }

        @Override
        public String toString() {
            return "BotKillContext[ownerUUID="
                    + ownerUUID
                    + ", botUUID="
                    + bot.getUniqueId()
                    + ", optionsOwner="
                    + (options == null ? null : options.getOwnerUUID())
                    + "]";
        }
    }

    public PlayerCheckListener(UltimateBot plugin) {
        this.plugin = plugin;
        this.botManager = plugin.getBotManager();
        this.playerOptions = plugin.getPlayerOptions();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        plugin.getWrapperManager().active().runEntityLater(player, 15L, () -> {
            BotBroadcaster.syncVisibleBotsForPlayer(
                    player, plugin.getBotRegistry().getAllBots().values());
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (botManager.isBotSpawned(player.getUniqueId())) {
            botManager.despawn(player, BotDespawnReason.OWNER_QUIT);
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

        Player player = event.getEntity();
        if (plugin.getBotRegistry().getOwnerUUIDByBotUUID(player.getUniqueId()) != null) {
            event.getDrops().clear();
            event.setDroppedExp(0);
            event.setKeepInventory(true);
        }
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
                botManager.despawn(player, BotDespawnReason.OWNER_DEATH);
                String despawnMsg = plugin.getLangString("messages.despawn-bot", "&cBot despawned!");
                player.sendMessage(ChatColorUtils.translate(despawnMsg));
                playerOptions.remove(player.getUniqueId());
            }
        }

        Entity killer = event.getEntity().getKiller();
        BotKillContext killContext = resolveBotKillContext(player, killer, wasBotSpawned, currentDeathMessage);

        if (killContext != null) {
            setBotDeathMessage(event, player, killContext.options());
            callBotKillEvent(player, killContext);
            return;
        }

        if (killer instanceof ITrainingBot) {
            setBotDeathMessage(event, player, options);
            return;
        }

        if (wasBotSpawned && currentDeathMessage != null) {
            ITrainingBot bot = botManager.getBot(player.getUniqueId());
            if (bot != null && currentDeathMessage.contains(bot.asBukkitPlayer().getName())) {
                setBotDeathMessage(event, player, options);
            }
        }
    }

    private void setBotDeathMessage(PlayerDeathEvent event, Player player, @Nullable BotOptions options) {
        if (options != null && !options.isKillMessageEnabled()) {

            event.setDeathMessage(null);
            return;
        }

        String configured = options == null ? null : options.getCustomKillMessage();
        String deathMessage = configured == null
                ? plugin.getLangString("messages.dead-bot-message", player.getName() + " was killed by his Bot")
                : configured;
        String formatted = ChatColorUtils.translate(deathMessage.replace("{player}", player.getName()));
        event.setDeathMessage(formatted);
    }

    private @Nullable String getDeathMessageText(PlayerDeathEvent event) {
        return event.getDeathMessage();
    }

    private @Nullable BotKillContext resolveBotKillContext(
            Player victim,
            org.bukkit.entity.@Nullable Entity killer,
            boolean victimOwnsBot,
            @Nullable String currentDeathMessage) {
        UUID killerUUID = killer == null ? null : killer.getUniqueId();
        if (killerUUID != null) {
            BotKillContext direct = findBotByBotUUID(killerUUID);
            if (direct != null) {
                return direct;
            }
            return null;
        }

        boolean crystalLikeKill = killer == null
                && currentDeathMessage != null
                && currentDeathMessage.contains("[Intentional Game Design]");

        for (Map.Entry<UUID, ITrainingBot> entry :
                plugin.getBotRegistry().getAllBots().entrySet()) {
            ITrainingBot candidate = entry.getValue();
            if (candidate == null || candidate.getTargetPlayer() == null) {
                continue;
            }
            if (candidate.getTargetPlayer().getUniqueId().equals(victim.getUniqueId())) {
                if (crystalLikeKill || deathMessageNamesBot(currentDeathMessage, candidate)) {
                    return toKillContext(entry.getKey(), candidate);
                }
            }
        }

        if (victimOwnsBot) {
            ITrainingBot ownedBot = botManager.getBotSafe(victim.getUniqueId());
            if (ownedBot != null && (crystalLikeKill || deathMessageNamesBot(currentDeathMessage, ownedBot))) {
                return toKillContext(victim.getUniqueId(), ownedBot);
            }
        }

        return null;
    }

    private boolean deathMessageNamesBot(@Nullable String deathMessage, ITrainingBot bot) {
        return deathMessage != null
                && bot != null
                && bot.asBukkitPlayer() != null
                && deathMessage.contains(bot.asBukkitPlayer().getName());
    }

    private @Nullable BotKillContext findBotByBotUUID(@Nullable UUID botUUID) {
        if (botUUID == null) {
            return null;
        }
        for (Map.Entry<UUID, ITrainingBot> entry :
                plugin.getBotRegistry().getAllBots().entrySet()) {
            ITrainingBot candidate = entry.getValue();
            if (candidate != null && candidate.asBukkitPlayer() != null && botUUID.equals(candidate.getUniqueId())) {
                return toKillContext(entry.getKey(), candidate);
            }
        }
        return null;
    }

    private BotKillContext toKillContext(UUID ownerUUID, ITrainingBot bot) {
        BotOptions options =
                bot.getBrainController() != null ? bot.getBrainController().getBotOptions() : null;
        return new BotKillContext(ownerUUID, bot, options);
    }

    private void callBotKillEvent(Player victim, BotKillContext context) {
        if (context == null || context.bot() == null || context.bot().asBukkitPlayer() == null) {
            return;
        }
        BotSnapshot snapshot = BotSnapshotMapper.toSnapshot(context.ownerUUID(), context.bot());
        if (snapshot == null) {
            return;
        }
        plugin.getServer()
                .getPluginManager()
                .callEvent(new BotKillPlayerEvent(
                        context.ownerUUID(), context.bot().getUniqueId(), victim, snapshot));
    }
}
