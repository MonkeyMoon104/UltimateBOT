package com.monkey.mcbot.metrics;

import com.monkey.mcbot.api.event.base.BotEvent;
import com.monkey.mcbot.bot.BotRegistry;
import com.monkey.mcbot.bot.ai.services.TargetingService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.bukkit.event.Cancellable;

/** Central low-cardinality instrumentation for the MinecraftBot runtime. */
public final class BotMetrics implements AutoCloseable {
    private static final String METRIC_PREFIX = "minecraftbot";

    private final boolean enabled;
    private final boolean prometheusEndpointEnabled;
    private final PrometheusMeterRegistry registry;
    private final Counter observerFailures;
    private final JvmGcMetrics jvmGcMetrics;

    public BotMetrics(
            boolean enabled,
            boolean prometheusEndpointEnabled,
            String pluginVersion,
            String minecraftVersion,
            BotRegistry botRegistry,
            TargetingService targetingService) {
        this.enabled = enabled;
        this.prometheusEndpointEnabled = enabled && prometheusEndpointEnabled;
        this.registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        this.registry
                .config()
                .commonTags(Tags.of(
                        "plugin_version", normalizedTag(pluginVersion),
                        "minecraft_version", normalizedTag(minecraftVersion)));

        if (enabled) {
            Gauge.builder(METRIC_PREFIX + ".bots.active", botRegistry, BotRegistry::size)
                    .description("Currently active managed bots")
                    .strongReference(true)
                    .register(registry);
            Gauge.builder(METRIC_PREFIX + ".cache.target.size", targetingService, TargetingService::estimatedCacheSize)
                    .description("Current target cache entry count")
                    .strongReference(true)
                    .register(registry);
            FunctionCounter.builder(
                            METRIC_PREFIX + ".cache.target.hits", targetingService, service -> service.cacheStats()
                                    .hitCount())
                    .description("Cumulative target cache hits")
                    .register(registry);
            FunctionCounter.builder(
                            METRIC_PREFIX + ".cache.target.misses", targetingService, service -> service.cacheStats()
                                    .missCount())
                    .description("Cumulative target cache misses")
                    .register(registry);
            FunctionCounter.builder(
                            METRIC_PREFIX + ".cache.target.evictions", targetingService, service -> service.cacheStats()
                                    .evictionCount())
                    .description("Cumulative target cache evictions")
                    .register(registry);

            new ClassLoaderMetrics().bindTo(registry);
            new JvmMemoryMetrics().bindTo(registry);
            new JvmThreadMetrics().bindTo(registry);
            new ProcessorMetrics().bindTo(registry);
            new UptimeMetrics().bindTo(registry);
            this.jvmGcMetrics = new JvmGcMetrics();
            this.jvmGcMetrics.bindTo(registry);
        } else {
            this.jvmGcMetrics = null;
        }

        this.observerFailures = Counter.builder(METRIC_PREFIX + ".events.observer.failures")
                .description("Bot event observer callback failures")
                .register(registry);
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
        boolean cancelled = event instanceof Cancellable cancellable && cancellable.isCancelled();
        Counter.builder(METRIC_PREFIX + ".events.published")
                .description("Published bot events")
                .tags("type", event.getClass().getSimpleName(), "cancelled", Boolean.toString(cancelled))
                .register(registry)
                .increment();
    }

    public void recordObserverFailure() {
        if (enabled) {
            observerFailures.increment();
        }
    }

    public void recordRemoteRequest(String method, String route, int status, long durationNanos) {
        if (!enabled) {
            return;
        }
        Timer.builder(METRIC_PREFIX + ".remote.requests")
                .description("Remote API request duration")
                .tags(
                        "method", normalizedTag(method).toUpperCase(Locale.ROOT),
                        "route", normalizedTag(route),
                        "status", Integer.toString(status > 0 ? status : 500))
                .publishPercentileHistogram()
                .register(registry)
                .record(Math.max(0L, durationNanos), TimeUnit.NANOSECONDS);
    }

    public String scrape() {
        return prometheusEndpointEnabled ? registry.scrape() : "";
    }

    @Override
    public void close() {
        if (jvmGcMetrics != null) {
            jvmGcMetrics.close();
        }
        registry.close();
    }

    private static String normalizedTag(String value) {
        String normalized = Objects.requireNonNullElse(value, "unknown").trim();
        return normalized.isEmpty() ? "unknown" : normalized;
    }
}
