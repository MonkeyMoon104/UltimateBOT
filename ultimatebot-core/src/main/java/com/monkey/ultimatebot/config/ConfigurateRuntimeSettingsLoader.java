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
                            "block-state"),
                    readWorldProtection(root.node("protection-world"), defaults.worldProtection()));
        } catch (RuntimeException | ConfigurateException exception) {
            logger.log(Level.WARNING, "Could not load typed runtime settings; defaults will be used", exception);
            return defaults;
        }
    }

    private RuntimeSettings.WorldProtectionSettings readWorldProtection(
            ConfigurationNode node, RuntimeSettings.WorldProtectionSettings defaults) {
        int lifetimeSeconds = nonNegativeOrDefault(
                node.node("combat-block-lifetime-seconds").getInt(defaults.combatBlockLifetimeSeconds()),
                defaults.combatBlockLifetimeSeconds(),
                "combat-block-lifetime-seconds");
        int maximumBlocks = positiveProtectionOrDefault(
                node.node("max-active-combat-blocks").getInt(defaults.maxActiveCombatBlocks()),
                defaults.maxActiveCombatBlocks(),
                "max-active-combat-blocks");
        return new RuntimeSettings.WorldProtectionSettings(
                node.node("block-damage").getBoolean(defaults.blockDamage()),
                node.node("anti-dupe").getBoolean(defaults.antiDupe()),
                lifetimeSeconds,
                maximumBlocks,
                node.node("respect-protection-plugins").getBoolean(defaults.respectProtectionPlugins()));
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

    private int nonNegativeOrDefault(int value, int fallback, String path) {
        if (value >= 0) {
            return value;
        }
        logger.warning("Invalid protection-world." + path + " value " + value + "; using " + fallback);
        return fallback;
    }

    private int positiveProtectionOrDefault(int value, int fallback, String path) {
        if (value > 0) {
            return value;
        }
        logger.warning("Invalid protection-world." + path + " value " + value + "; using " + fallback);
        return fallback;
    }
}
