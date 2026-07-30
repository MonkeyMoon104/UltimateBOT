package com.monkey.mcbot.common.metrics;

import java.util.Objects;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;

/** Immutable Java-only runtime values exposed to an optional metrics backend. */
public record MetricsBackendContext(
        boolean prometheusEndpointEnabled,
        String pluginVersion,
        String minecraftVersion,
        IntSupplier activeBots,
        LongSupplier targetCacheSize,
        LongSupplier targetCacheHits,
        LongSupplier targetCacheMisses,
        LongSupplier targetCacheEvictions) {
    public MetricsBackendContext {
        Objects.requireNonNull(pluginVersion, "pluginVersion");
        Objects.requireNonNull(minecraftVersion, "minecraftVersion");
        Objects.requireNonNull(activeBots, "activeBots");
        Objects.requireNonNull(targetCacheSize, "targetCacheSize");
        Objects.requireNonNull(targetCacheHits, "targetCacheHits");
        Objects.requireNonNull(targetCacheMisses, "targetCacheMisses");
        Objects.requireNonNull(targetCacheEvictions, "targetCacheEvictions");
    }
}
