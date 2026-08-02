package com.monkey.ultimatebot.config;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

public final class ConfigurateRuntimeSettingsLoader {

    private final Path configurationPath;
    private final Logger logger;

    public ConfigurateRuntimeSettingsLoader(Path configurationPath, Logger logger) {
        this.configurationPath = Objects.requireNonNull(configurationPath, "configurationPath");
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    public RuntimeSettings load() {
        RuntimeSettings defaults = RuntimeSettings.defaults();
        try {
            ConfigurationNode root = YamlConfigurationLoader.builder()
                    .path(configurationPath)
                    .build()
                    .load();
            return new RuntimeSettings(
                    readCache(root.node("performance", "caches", "target"), defaults.targetCache(), "target"),
                    readCache(
                            root.node("performance", "caches", "block-state"),
                            defaults.blockStateCache(),
                            "block-state"));
        } catch (RuntimeException | ConfigurateException exception) {
            logger.log(Level.WARNING, "Could not load typed performance settings; defaults will be used", exception);
            return defaults;
        }
    }

    private RuntimeSettings.CacheSettings readCache(
            ConfigurationNode node, RuntimeSettings.CacheSettings defaults, String cacheName) {
        long maximumSize = positiveOrDefault(
                node.node("maximum-size").getLong(defaults.maximumSize()),
                defaults.maximumSize(),
                cacheName + ".maximum-size");
        long expiryMillis = positiveOrDefault(
                node.node("expire-after-write-ms")
                        .getLong(defaults.expireAfterWrite().toMillis()),
                defaults.expireAfterWrite().toMillis(),
                cacheName + ".expire-after-write-ms");
        return new RuntimeSettings.CacheSettings(maximumSize, Duration.ofMillis(expiryMillis));
    }

    private long positiveOrDefault(long value, long fallback, String path) {
        if (value > 0) {
            return value;
        }
        logger.warning("Invalid performance.caches." + path + " value " + value + "; using " + fallback);
        return fallback;
    }
}
