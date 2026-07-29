package com.monkey.mcbot.config;

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
        Files.writeString(configuration, """
                performance:
                  caches:
                    target:
                      maximum-size: 512
                      expire-after-write-ms: 125
                    block-state:
                      maximum-size: 4096
                      expire-after-write-ms: 2000
                """);

        RuntimeSettings settings = new ConfigurateRuntimeSettingsLoader(
                        configuration, Logger.getLogger(ConfigurateRuntimeSettingsLoaderTest.class.getName()))
                .load();

        assertThat(settings.targetCache().maximumSize()).isEqualTo(512);
        assertThat(settings.targetCache().expireAfterWrite()).isEqualTo(Duration.ofMillis(125));
        assertThat(settings.blockStateCache().maximumSize()).isEqualTo(4_096);
        assertThat(settings.blockStateCache().expireAfterWrite()).isEqualTo(Duration.ofSeconds(2));
    }

    @Test
    void fallsBackPerInvalidValue() throws IOException {
        Path configuration = temporaryDirectory.resolve("invalid.yml");
        Files.writeString(configuration, """
                performance:
                  caches:
                    target:
                      maximum-size: 0
                      expire-after-write-ms: -1
                """);

        RuntimeSettings settings = new ConfigurateRuntimeSettingsLoader(
                        configuration, Logger.getLogger(ConfigurateRuntimeSettingsLoaderTest.class.getName()))
                .load();

        assertThat(settings.targetCache()).isEqualTo(RuntimeSettings.defaults().targetCache());
    }
}
