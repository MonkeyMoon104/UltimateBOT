package com.monkey.ultimatebot.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConfigurateRuntimeSettingsLoaderTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void loadsTypedCacheSettingsFromYaml() throws IOException {
        Path configuration = temporaryDirectory.resolve("config.yml");
        Files.writeString(
                configuration,
                """
                performance:
                  caches:
                    target:
                      maximum-size: 512
                      expire-after-write-ms: 125
                    block-state:
                      maximum-size: 4096
                      expire-after-write-ms: 2000
                protection-world:
                  allow-bot-explosion-block-damage: true
                  anti-dupe: false
                  combat-block-lifetime-seconds: 45
                  max-active-combat-blocks: 512
                  combat-entity-lifetime-seconds: 90
                  max-active-combat-entities: 128
                  respect-protection-plugins: false
                vanilla-statistics:
                  kills: false
                  deaths: false
                """);

        RuntimeSettings settings = new ConfigurateRuntimeSettingsLoader(
                        configuration, Logger.getLogger(ConfigurateRuntimeSettingsLoaderTest.class.getName()))
                .load();

        assertThat(settings.targetCache().maximumSize()).isEqualTo(512);
        assertThat(settings.targetCache().expireAfterWrite()).isEqualTo(Duration.ofMillis(125));
        assertThat(settings.blockStateCache().maximumSize()).isEqualTo(4_096);
        assertThat(settings.blockStateCache().expireAfterWrite()).isEqualTo(Duration.ofSeconds(2));
        assertThat(settings.worldProtection())
                .isEqualTo(new RuntimeSettings.WorldProtectionSettings(true, false, 45, 512, 90, 128, false));
        assertThat(settings.vanillaStatistics().trackKills()).isFalse();
        assertThat(settings.vanillaStatistics().trackDeaths()).isFalse();
        assertThat(settings.configVersion()).isEqualTo(1);
    }

    @Test
    void supportsLegacyExplosionBlockDamagePermission() throws IOException {
        Path configuration = temporaryDirectory.resolve("legacy-config.yml");
        Files.writeString(
                configuration,
                """
                protection-world:
                  block-damage: true
                """);

        RuntimeSettings settings = new ConfigurateRuntimeSettingsLoader(
                        configuration, Logger.getLogger(ConfigurateRuntimeSettingsLoaderTest.class.getName()))
                .load();

        assertThat(settings.worldProtection().allowBotExplosionBlockDamage()).isTrue();
    }

    @Test
    void fallsBackPerInvalidValue() throws IOException {
        Path configuration = temporaryDirectory.resolve("invalid.yml");
        Files.writeString(
                configuration,
                """
                performance:
                  caches:
                    target:
                      maximum-size: 0
                      expire-after-write-ms: -1
                protection-world:
                  combat-block-lifetime-seconds: -1
                  max-active-combat-blocks: 0
                  combat-entity-lifetime-seconds: -1
                  max-active-combat-entities: 0
                """);

        RuntimeSettings settings = new ConfigurateRuntimeSettingsLoader(
                        configuration, Logger.getLogger(ConfigurateRuntimeSettingsLoaderTest.class.getName()))
                .load();

        assertThat(settings.targetCache()).isEqualTo(RuntimeSettings.defaults().targetCache());
        assertThat(settings.worldProtection())
                .isEqualTo(RuntimeSettings.defaults().worldProtection());
    }
}
