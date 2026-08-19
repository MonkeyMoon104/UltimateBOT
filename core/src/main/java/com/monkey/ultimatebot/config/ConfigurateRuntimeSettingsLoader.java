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
    private ConfigLoadReport lastReport = new ConfigLoadReport();

    public ConfigurateRuntimeSettingsLoader(Path configurationPath, Logger logger) {
        this.configurationPath = Objects.requireNonNull(configurationPath, "configurationPath");
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    public ConfigLoadReport lastReport() {
        return lastReport;
    }

    public RuntimeSettings load() {
        ConfigLoadReport report = new ConfigLoadReport();
        this.lastReport = report;
        RuntimeSettings defaults = RuntimeSettings.defaults();
        try {
            ConfigurationNode root = YamlConfigurationLoader.builder()
                    .path(configurationPath)
                    .build()
                    .load();
            return new RuntimeSettings(
                    readCache(root.node("performance", "caches", "target"), defaults.targetCache(), "target", report),
                    readCache(
                            root.node("performance", "caches", "block-state"),
                            defaults.blockStateCache(),
                            "block-state", report),
                    readWorldProtection(root.node("protection-world"), defaults.worldProtection(), report));
        } catch (RuntimeException | ConfigurateException exception) {
            logger.log(Level.WARNING, "Could not load typed runtime settings; defaults will be used", exception);
            return defaults;
        }
    }

    private RuntimeSettings.WorldProtectionSettings readWorldProtection(
            ConfigurationNode node, RuntimeSettings.WorldProtectionSettings defaults, ConfigLoadReport report) {
        report.incrementKeysRead(); // combat-block-lifetime-seconds
        report.incrementKeysRead(); // max-active-combat-blocks
        report.incrementKeysRead(); // combat-entity-lifetime-seconds
        report.incrementKeysRead(); // max-active-combat-entities
        report.incrementKeysRead(); // allow-bot-explosion-block-damage
        report.incrementKeysRead(); // anti-dupe
        report.incrementKeysRead(); // respect-protection-plugins
        int lifetimeSeconds = nonNegativeOrDefault(
                node.node("combat-block-lifetime-seconds").getInt(defaults.combatBlockLifetimeSeconds()),
                defaults.combatBlockLifetimeSeconds(),
                "combat-block-lifetime-seconds", report);
        int maximumBlocks = positiveProtectionOrDefault(
                node.node("max-active-combat-blocks").getInt(defaults.maxActiveCombatBlocks()),
                defaults.maxActiveCombatBlocks(),
                "max-active-combat-blocks", report);
        int entityLifetimeSeconds = nonNegativeOrDefault(
                node.node("combat-entity-lifetime-seconds").getInt(defaults.combatEntityLifetimeSeconds()),
                defaults.combatEntityLifetimeSeconds(),
                "combat-entity-lifetime-seconds", report);
        int maximumEntities = positiveProtectionOrDefault(
                node.node("max-active-combat-entities").getInt(defaults.maxActiveCombatEntities()),
                defaults.maxActiveCombatEntities(),
                "max-active-combat-entities", report);
        return new RuntimeSettings.WorldProtectionSettings(
                readExplosionBlockDamagePermission(node, defaults.allowBotExplosionBlockDamage()),
                node.node("anti-dupe").getBoolean(defaults.antiDupe()),
                lifetimeSeconds,
                maximumBlocks,
                entityLifetimeSeconds,
                maximumEntities,
                node.node("respect-protection-plugins").getBoolean(defaults.respectProtectionPlugins()));
    }

    private boolean readExplosionBlockDamagePermission(ConfigurationNode node, boolean fallback) {
        ConfigurationNode current = node.node("allow-bot-explosion-block-damage");
        if (current.raw() != null) {
            return current.getBoolean(fallback);
        }
        return node.node("block-damage").getBoolean(fallback);
    }

    private RuntimeSettings.CacheSettings readCache(
            ConfigurationNode node, RuntimeSettings.CacheSettings defaults, String cacheName, ConfigLoadReport report) {
        report.incrementKeysRead(); // maximum-size
        report.incrementKeysRead(); // expire-after-write-ms
        long maximumSize = positiveOrDefault(
                node.node("maximum-size").getLong(defaults.maximumSize()),
                defaults.maximumSize(),
                cacheName + ".maximum-size", report);
        long expiryMillis = positiveOrDefault(
                node.node("expire-after-write-ms")
                        .getLong(defaults.expireAfterWrite().toMillis()),
                defaults.expireAfterWrite().toMillis(),
                cacheName + ".expire-after-write-ms", report);
        return new RuntimeSettings.CacheSettings(maximumSize, Duration.ofMillis(expiryMillis));
    }

    private long positiveOrDefault(long value, long fallback, String path, ConfigLoadReport report) {
        if (value > 0) {
            return value;
        }
        report.addInvalidKey("performance.caches." + path, value, fallback);
        logger.warning("Invalid performance.caches." + path + " value " + value + "; using " + fallback);
        return fallback;
    }

    private int nonNegativeOrDefault(int value, int fallback, String path, ConfigLoadReport report) {
        if (value >= 0) {
            return value;
        }
        report.addInvalidKey("protection-world." + path, value, fallback);
        logger.warning("Invalid protection-world." + path + " value " + value + "; using " + fallback);
        return fallback;
    }

    private int positiveProtectionOrDefault(int value, int fallback, String path, ConfigLoadReport report) {
        if (value > 0) {
            return value;
        }
        report.addInvalidKey("protection-world." + path, value, fallback);
        logger.warning("Invalid protection-world." + path + " value " + value + "; using " + fallback);
        return fallback;
    }
}
