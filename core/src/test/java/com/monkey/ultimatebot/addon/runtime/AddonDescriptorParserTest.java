package com.monkey.ultimatebot.addon.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AddonDescriptorParserTest {
    @TempDir
    private Path temporaryDirectory;

    @Test
    void readsValidatedDescriptorAndNativeEntrypoints() throws Exception {
        Properties descriptor = validDescriptor();
        descriptor.setProperty("native.1.21.11", "example.NativeAddon");
        Path jar = createJar(descriptor, false);

        var parsed = Objects.requireNonNull(AddonDescriptorParser.parse(jar), "parsed descriptor");

        assertThat(parsed.id()).isEqualTo("example-ai");
        assertThat(parsed.nativeProviders()).containsEntry("1.21.11", "example.NativeAddon");
    }

    @Test
    void rejectsAddonsThatBundleThePublicApi() throws Exception {
        Path jar = createJar(validDescriptor(), true);

        assertThatThrownBy(() -> AddonDescriptorParser.validateJarContents(jar))
                .isInstanceOf(AddonLoadException.class)
                .hasMessageContaining("compileOnly");
    }

    private Path createJar(Properties descriptor, boolean bundlesApi) throws IOException {
        Path jar = temporaryDirectory.resolve("addon-" + System.nanoTime() + ".jar");
        try (OutputStream output = Files.newOutputStream(jar);
                ZipOutputStream zip = new ZipOutputStream(output)) {
            zip.putNextEntry(new ZipEntry(AddonDescriptorParser.DESCRIPTOR_PATH));
            descriptor.store(zip, "test");
            zip.closeEntry();
            if (bundlesApi) {
                zip.putNextEntry(new ZipEntry("com/monkey/ultimatebot/api/Fake.class"));
                zip.write(new byte[] {0});
                zip.closeEntry();
            }
        }
        return jar;
    }

    private static Properties validDescriptor() {
        Properties descriptor = new Properties();
        descriptor.setProperty("id", "example-ai");
        descriptor.setProperty("name", "Example AI");
        descriptor.setProperty("version", "1.0.0");
        descriptor.setProperty("api-version", "2");
        descriptor.setProperty("main", "example.ExampleAddon");
        descriptor.setProperty("authors", "Developer");
        return descriptor;
    }
}
