package com.monkey.ultimatebot.common.metrics;

import java.util.Objects;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;

public final class MetricsBackendContext {
    private final boolean prometheusEndpointEnabled;
    private final String pluginVersion;
    private final String minecraftVersion;
    private final IntSupplier activeBots;
    private final LongSupplier targetCacheSize;
    private final LongSupplier targetCacheHits;
    private final LongSupplier targetCacheMisses;
    private final LongSupplier targetCacheEvictions;

    public MetricsBackendContext(
            boolean prometheusEndpointEnabled,
            String pluginVersion,
            String minecraftVersion,
            IntSupplier activeBots,
            LongSupplier targetCacheSize,
            LongSupplier targetCacheHits,
            LongSupplier targetCacheMisses,
            LongSupplier targetCacheEvictions) {
        this.prometheusEndpointEnabled = prometheusEndpointEnabled;
        this.pluginVersion = Objects.requireNonNull(pluginVersion, "pluginVersion");
        this.minecraftVersion = Objects.requireNonNull(minecraftVersion, "minecraftVersion");
        this.activeBots = Objects.requireNonNull(activeBots, "activeBots");
        this.targetCacheSize = Objects.requireNonNull(targetCacheSize, "targetCacheSize");
        this.targetCacheHits = Objects.requireNonNull(targetCacheHits, "targetCacheHits");
        this.targetCacheMisses = Objects.requireNonNull(targetCacheMisses, "targetCacheMisses");
        this.targetCacheEvictions = Objects.requireNonNull(targetCacheEvictions, "targetCacheEvictions");
    }

    public boolean prometheusEndpointEnabled() {
        return prometheusEndpointEnabled;
    }

    public String pluginVersion() {
        return pluginVersion;
    }

    public String minecraftVersion() {
        return minecraftVersion;
    }

    public IntSupplier activeBots() {
        return activeBots;
    }

    public LongSupplier targetCacheSize() {
        return targetCacheSize;
    }

    public LongSupplier targetCacheHits() {
        return targetCacheHits;
    }

    public LongSupplier targetCacheMisses() {
        return targetCacheMisses;
    }

    public LongSupplier targetCacheEvictions() {
        return targetCacheEvictions;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MetricsBackendContext)) {
            return false;
        }
        MetricsBackendContext other = (MetricsBackendContext) obj;
        return prometheusEndpointEnabled == other.prometheusEndpointEnabled
                && pluginVersion.equals(other.pluginVersion)
                && minecraftVersion.equals(other.minecraftVersion)
                && activeBots.equals(other.activeBots)
                && targetCacheSize.equals(other.targetCacheSize)
                && targetCacheHits.equals(other.targetCacheHits)
                && targetCacheMisses.equals(other.targetCacheMisses)
                && targetCacheEvictions.equals(other.targetCacheEvictions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                prometheusEndpointEnabled,
                pluginVersion,
                minecraftVersion,
                activeBots,
                targetCacheSize,
                targetCacheHits,
                targetCacheMisses,
                targetCacheEvictions);
    }

    @Override
    public String toString() {
        return "MetricsBackendContext[prometheusEndpointEnabled="
                + prometheusEndpointEnabled
                + ", pluginVersion="
                + pluginVersion
                + ", minecraftVersion="
                + minecraftVersion
                + ", activeBots="
                + activeBots
                + ", targetCacheSize="
                + targetCacheSize
                + ", targetCacheHits="
                + targetCacheHits
                + ", targetCacheMisses="
                + targetCacheMisses
                + ", targetCacheEvictions="
                + targetCacheEvictions
                + ']';
    }
}
