package com.monkey.ultimatebot.metrics;

import com.monkey.ultimatebot.api.event.base.BotEvent;
import com.monkey.ultimatebot.bot.BotRegistry;
import com.monkey.ultimatebot.bot.ai.services.TargetingService;
import com.monkey.ultimatebot.common.addon.AddonDefinition;
import com.monkey.ultimatebot.common.addon.AddonLoader;
import com.monkey.ultimatebot.common.addon.LoadedAddon;
import com.monkey.ultimatebot.common.metrics.MetricsBackend;
import com.monkey.ultimatebot.common.metrics.MetricsBackendContext;
import com.monkey.ultimatebot.common.metrics.MetricsBackendFactory;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.event.Cancellable;
import org.jspecify.annotations.Nullable;

/** Lightweight facade that initializes Micrometer and Prometheus only when explicitly enabled at startup. */
public final class BotMetrics implements AutoCloseable {
    private static final AddonDefinition ADDON = new AddonDefinition(
            "metrics",
            "UltimateBot metrics addon",
            "UltimateBot-Metrics.jar",
            "META-INF/ultimatebot/addons/metrics.properties",
            "ultimatebot.addons.metrics.url");

    private final boolean enabled;
    private final boolean prometheusEndpointEnabled;
    private final MetricsBackend backend;
    private final @Nullable LoadedAddon<MetricsBackend> loadedAddon;

    public BotMetrics(
            boolean enabled,
            boolean prometheusEndpointEnabled,
            String pluginVersion,
            String minecraftVersion,
            BotRegistry botRegistry,
            TargetingService targetingService,
            Path pluginDataDirectory,
            ClassLoader pluginClassLoader,
            Logger logger) {
        Objects.requireNonNull(botRegistry, "botRegistry");
        Objects.requireNonNull(targetingService, "targetingService");
        Objects.requireNonNull(logger, "logger");

        if (!enabled) {
            this.enabled = false;
            this.prometheusEndpointEnabled = false;
            this.backend = MetricsBackend.NOOP;
            this.loadedAddon = null;
            return;
        }

        MetricsBackendContext context = new MetricsBackendContext(
                prometheusEndpointEnabled,
                pluginVersion,
                minecraftVersion,
                botRegistry::size,
                targetingService::estimatedCacheSize,
                () -> targetingService.cacheStats().hitCount(),
                () -> targetingService.cacheStats().missCount(),
                () -> targetingService.cacheStats().evictionCount());
        LoadedAddon<MetricsBackend> loaded;
        try {
            loaded = new AddonLoader(pluginDataDirectory, pluginClassLoader, logger)
                    .load(ADDON, MetricsBackendFactory.class, factory -> factory.create(context));
        } catch (Exception | LinkageError error) {
            logger.warning("Metrics addon unavailable; observability disabled -> " + error.getMessage());
            logger.log(Level.FINE, "Metrics addon startup failure", error);
            this.enabled = false;
            this.prometheusEndpointEnabled = false;
            this.backend = MetricsBackend.NOOP;
            this.loadedAddon = null;
            return;
        }

        this.enabled = true;
        this.prometheusEndpointEnabled = prometheusEndpointEnabled;
        this.loadedAddon = loaded;
        this.backend = loaded.instance();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isPrometheusEndpointEnabled() {
        return prometheusEndpointEnabled;
    }

    public void recordEvent(BotEvent event) {
        if (!enabled) {
            return;
        }
        boolean cancelled = event instanceof Cancellable && ((Cancellable) event).isCancelled();
        backend.recordEvent(event.getClass().getSimpleName(), cancelled);
    }

    public void recordObserverFailure() {
        if (enabled) {
            backend.recordObserverFailure();
        }
    }

    public void recordRemoteRequest(String method, String route, int status, long durationNanos) {
        if (enabled) {
            backend.recordRemoteRequest(method, route, status, durationNanos);
        }
    }

    public String scrape() {
        return prometheusEndpointEnabled ? backend.scrape() : "";
    }

    @Override
    public void close() {
        if (loadedAddon != null) {
            loadedAddon.close();
        }
    }
}
