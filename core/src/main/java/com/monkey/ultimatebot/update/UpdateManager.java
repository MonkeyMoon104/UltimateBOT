package com.monkey.ultimatebot.update;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.compat.PluginMetaAccess;
import com.monkey.ultimatebot.compat.UpdateNotifyAccess;
import com.monkey.ultimatebot.logging.UltimateBotLogging;
import com.monkey.ultimatebot.wrapper.WrapperTask;
import java.io.IOException;
import java.util.Collection;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jspecify.annotations.Nullable;

public final class UpdateManager implements Listener {

    private static final String PRODUCT_CODE = "ultimatebot";
    private static final String DEFAULT_DOWNLOAD_URL =
            "https://builtbybit.com/resources/ultimatebot-pvp-practice-bots.100308/";

    private final UltimateBot plugin;
    private final UpdateCheckHttpClient updateHttpClient;
    private final long joinNotifyDelayTicks;
    private final boolean periodicBroadcastEnabled;
    private final long periodicIntervalTicks;

    private volatile @Nullable UpdateState lastUpdateState;
    private @Nullable WrapperTask periodicTask;

    public UpdateManager(UltimateBot plugin) {
        this.plugin = plugin;
        this.updateHttpClient = new UpdateCheckHttpClient(new ObjectMapper());

        long joinDelaySeconds = Math.max(1L, plugin.getConfig().getLong("updates.join-notify-delay-seconds", 5L));
        this.joinNotifyDelayTicks = joinDelaySeconds * 20L;

        ConfigurationSection periodicSection = plugin.getConfig().getConfigurationSection("updates.periodic-broadcast");
        this.periodicBroadcastEnabled = periodicSection == null || periodicSection.getBoolean("enabled", true);

        long intervalMinutes =
                periodicSection == null ? 30L : Math.max(1L, periodicSection.getLong("interval-minutes", 30L));
        this.periodicIntervalTicks = intervalMinutes * 60L * 20L;
    }

    public UpdateStartupResult checkOnStartup() {
        try {
            PluginUpdateCheckResponse response = checkRemote();
            this.lastUpdateState = toState(response);

            if (response.updateAvailable()) {
                return new UpdateStartupResult(
                        false,
                        true,
                        "New version available -> current=" + response.currentVersion() + ", latest="
                                + response.latestVersion());
            }
            return new UpdateStartupResult(false, false, "Up to date -> current=" + response.currentVersion());
        } catch (IOException ex) {
            return new UpdateStartupResult(true, false, "Update check failed -> " + safeMessage(ex.getMessage()));
        }
    }

    public void startRuntime() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        if (!periodicBroadcastEnabled) {
            return;
        }

        periodicTask = plugin.getWrapperManager()
                .active()
                .runAsyncRepeating(this::runPeriodicCheck, periodicIntervalTicks, periodicIntervalTicks);
    }

    public void shutdown() {
        if (periodicTask != null) {
            periodicTask.cancel();
            periodicTask = null;
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!isAdmin(player)) {
            return;
        }

        plugin.getWrapperManager()
                .active()
                .runEntityLater(player, joinNotifyDelayTicks, () -> notifyPlayerIfUpdateAvailable(player));
    }

    private void notifyPlayerIfUpdateAvailable(Player player) {
        if (!player.isOnline()) {
            return;
        }
        UpdateState state = lastUpdateState;
        if (state == null || !state.updateAvailable()) {
            return;
        }
        UpdateNotifyAccess.sendUpdateAvailable(
                player, state.currentVersion(), state.latestVersion(), state.downloadUrl());
    }

    private void runPeriodicCheck() {
        try {
            PluginUpdateCheckResponse response = checkRemote();
            UpdateState state = toState(response);
            this.lastUpdateState = state;

            if (!state.updateAvailable()) {
                return;
            }

            plugin.getWrapperManager().active().runSync(() -> broadcastToAdmins(state));
        } catch (IOException ex) {
            UltimateBotLogging.warn(
                    plugin.getLogger(), "Update", "Periodic check failed -> " + safeMessage(ex.getMessage()));
        }
    }

    private void broadcastToAdmins(UpdateState state) {
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        for (Player player : players) {
            if (isAdmin(player)) {
                UpdateNotifyAccess.sendUpdateAvailable(
                player, state.currentVersion(), state.latestVersion(), state.downloadUrl());
            }
        }
    }

    private PluginUpdateCheckResponse checkRemote() throws IOException {
        return updateHttpClient.check(new PluginUpdateCheckRequest(
                PRODUCT_CODE, PluginMetaAccess.version(plugin)));
    }

    private UpdateState toState(PluginUpdateCheckResponse response) {
        String url = response.downloadUrl() == null || response.downloadUrl().trim().isEmpty()
                ? DEFAULT_DOWNLOAD_URL
                : response.downloadUrl();

        return new UpdateState(
                response.updateAvailable(),
                safeMessage(response.currentVersion()),
                safeMessage(response.latestVersion()),
                url,
                safeMessage(response.message()));
    }

    private boolean isAdmin(Player player) {
        return player.isOp() || player.hasPermission("ultimatebot.admin.use");
    }

    private String safeMessage(@Nullable String value) {
        return value == null || value.trim().isEmpty() ? "n/a" : value;
    }
}
