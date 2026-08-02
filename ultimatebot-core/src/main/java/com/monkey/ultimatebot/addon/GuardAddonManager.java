package com.monkey.ultimatebot.addon;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.BotRegistry;
import com.monkey.ultimatebot.common.addon.AddonDefinition;
import com.monkey.ultimatebot.common.addon.AddonLoader;
import com.monkey.ultimatebot.common.addon.LoadedAddon;
import com.monkey.ultimatebot.common.guard.GuardBackend;
import com.monkey.ultimatebot.common.guard.GuardBackendContext;
import com.monkey.ultimatebot.common.guard.GuardBackendFactory;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.event.Listener;
import org.jspecify.annotations.Nullable;

/** Owns the lifecycle of the automatically installed guard addon. */
public final class GuardAddonManager implements AutoCloseable {
    private static final AddonDefinition ADDON = new AddonDefinition(
            "guard",
            "UltimateBot guard addon",
            "UltimateBot-Guard.jar",
            "META-INF/ultimatebot/addons/guard.properties",
            "ultimatebot.addons.guard.url");

    private final UltimateBot plugin;
    private final BotRegistry botRegistry;
    private @Nullable LoadedAddon<GuardBackend> loadedAddon;
    private GuardBackend backend = GuardBackend.NOOP;

    public GuardAddonManager(UltimateBot plugin, BotRegistry botRegistry) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.botRegistry = Objects.requireNonNull(botRegistry, "botRegistry");
    }

    public boolean load() {
        if (loadedAddon != null) {
            return true;
        }

        GuardBackendContext context = new GuardBackendContext(
                plugin,
                uuid -> botRegistry.getOwnerUUIDByBotUUID(uuid) != null,
                listener -> plugin.getServer().getPluginManager().registerEvents(requireListener(listener), plugin));
        try {
            loadedAddon = new AddonLoader(
                            plugin.getDataFolder().toPath(), plugin.getClass().getClassLoader(), plugin.getLogger())
                    .load(ADDON, GuardBackendFactory.class, factory -> factory.create(context));
            backend = loadedAddon.instance();
            return true;
        } catch (Exception | LinkageError error) {
            plugin.getLogger()
                    .warning("Guard addon unavailable; compatibility protection disabled -> " + error.getMessage());
            plugin.getLogger().log(Level.FINE, "Guard addon startup failure", error);
            return false;
        }
    }

    public void markBot(Object entity) {
        backend.markBot(entity);
    }

    public void forgetBot(UUID entityUuid) {
        backend.forgetBot(entityUuid);
    }

    public boolean isLoaded() {
        return loadedAddon != null;
    }

    @Override
    public void close() {
        if (loadedAddon != null) {
            loadedAddon.close();
            loadedAddon = null;
        }
        backend = GuardBackend.NOOP;
    }

    private static Listener requireListener(Object listener) {
        if (listener instanceof Listener bukkitListener) {
            return bukkitListener;
        }
        throw new IllegalArgumentException("Guard addon listener does not implement Bukkit Listener");
    }
}
