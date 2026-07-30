package com.monkey.mcbot.metrics.addon;

import com.monkey.mcbot.common.metrics.MetricsBackend;
import com.monkey.mcbot.common.metrics.MetricsBackendContext;
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

/** Micrometer implementation kept outside the main plugin classpath. */
final class MicrometerMetricsBackend implements MetricsBackend {
    private static final String METRIC_PREFIX = "minecraftbot";

    private final boolean prometheusEndpointEnabled;
    private final PrometheusMeterRegistry registry;
    private final Counter observerFailures;
    private final JvmGcMetrics jvmGcMetrics;

    MicrometerMetricsBackend(MetricsBackendContext context) {
        this.prometheusEndpointEnabled = context.prometheusEndpointEnabled();
        this.registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        this.registry
                .config()
                .commonTags(Tags.of(
                        "plugin_version", normalizedTag(context.pluginVersion()),
                        "minecraft_version", normalizedTag(context.minecraftVersion())));

        Gauge.builder(METRIC_PREFIX + ".bots.active", context.activeBots(), supplier -> supplier.getAsInt())
                .description("Currently active managed bots")
                .strongReference(true)
                .register(registry);
        Gauge.builder(METRIC_PREFIX + ".cache.target.size", context.targetCacheSize(), supplier -> supplier.getAsLong())
                .description("Current target cache entry count")
                .strongReference(true)
                .register(registry);
        FunctionCounter.builder(
                        METRIC_PREFIX + ".cache.target.hits",
                        context.targetCacheHits(),
                        supplier -> supplier.getAsLong())
                .description("Cumulative target cache hits")
                .register(registry);
        FunctionCounter.builder(
                        METRIC_PREFIX + ".cache.target.misses",
                        context.targetCacheMisses(),
                        supplier -> supplier.getAsLong())
                .description("Cumulative target cache misses")
                .register(registry);
        FunctionCounter.builder(
                        METRIC_PREFIX + ".cache.target.evictions",
                        context.targetCacheEvictions(),
                        supplier -> supplier.getAsLong())
                .description("Cumulative target cache evictions")
                .register(registry);

        new ClassLoaderMetrics().bindTo(registry);
        new JvmMemoryMetrics().bindTo(registry);
        new JvmThreadMetrics().bindTo(registry);
        new ProcessorMetrics().bindTo(registry);
        new UptimeMetrics().bindTo(registry);
        this.jvmGcMetrics = new JvmGcMetrics();
        this.jvmGcMetrics.bindTo(registry);

        this.observerFailures = Counter.builder(METRIC_PREFIX + ".events.observer.failures")
                .description("Bot event observer callback failures")
                .register(registry);
    }

    @Override
    public void recordEvent(String eventType, boolean cancelled) {
        Counter.builder(METRIC_PREFIX + ".events.published")
                .description("Published bot events")
                .tags("type", eventType, "cancelled", Boolean.toString(cancelled))
                .register(registry)
                .increment();
    }

    @Override
    public void recordObserverFailure() {
        observerFailures.increment();
    }

    @Override
    public void recordRemoteRequest(String method, String route, int status, long durationNanos) {
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

    @Override
    public String scrape() {
        return prometheusEndpointEnabled ? registry.scrape() : "";
    }

    @Override
    public void close() {
        jvmGcMetrics.close();
        registry.close();
    }

    private static String normalizedTag(String value) {
        String normalized = Objects.requireNonNullElse(value, "unknown").trim();
        return normalized.isEmpty() ? "unknown" : normalized;
    }
}
