package com.monkey.ultimatebot.common.addon;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AddonLoaderTest {
    private static final String DESCRIPTOR = "test/addon.properties";
    private static final byte[] ADDON_BYTES = "verified-addon".getBytes(StandardCharsets.UTF_8);

    @TempDir
    Path temporaryDirectory;

    @Test
    void downloadsValidatesAndReusesAnInstalledAddon() throws Exception {
        AtomicInteger downloads = new AtomicInteger();
        AddonLoader.AddonDownloader downloader = (source, destination, version) -> {
            downloads.incrementAndGet();
            Files.write(destination, ADDON_BYTES);
        };
        ClassLoader descriptorLoader = new DescriptorClassLoader(getClass().getClassLoader(), descriptor());
        AddonLoader loader = new AddonLoader(
                temporaryDirectory, descriptorLoader, Logger.getLogger(AddonLoaderTest.class.getName()), downloader);
        AddonDefinition definition =
                new AddonDefinition("test", "test addon", "Test-Addon.jar", DESCRIPTOR, "test.addon.url");

        try (LoadedAddon<TestAddon> first = loader.load(definition, TestFactory.class, TestFactory::create);
                LoadedAddon<TestAddon> second = loader.load(definition, TestFactory.class, TestFactory::create)) {
            assertThat(first.instance().active()).isTrue();
            assertThat(second.instance().active()).isTrue();
        }

        assertThat(downloads).hasValue(1);
        assertThat(temporaryDirectory.resolve("addon/Test-Addon.jar")).hasBinaryContent(ADDON_BYTES);
    }

    private static byte[] descriptor() throws Exception {
        String sha256 =
                HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(ADDON_BYTES));
        return ("version=1.5.0\n"
                        + "url=https://example.invalid/Test-Addon.jar\n"
                        + "sha256="
                        + sha256
                        + "\nsize="
                        + ADDON_BYTES.length
                        + "\nfactory-class="
                        + TestFactory.class.getName()
                        + "\n")
                .getBytes(StandardCharsets.UTF_8);
    }

    public static final class TestFactory {
        public TestAddon create() {
            return new TestAddon();
        }
    }

    static final class TestAddon implements AutoCloseable {
        private boolean active = true;

        boolean active() {
            return active;
        }

        @Override
        public void close() {
            active = false;
        }
    }

    private static final class DescriptorClassLoader extends ClassLoader {
        private final byte[] descriptor;

        private DescriptorClassLoader(ClassLoader parent, byte[] descriptor) {
            super(parent);
            this.descriptor = descriptor.clone();
        }

        @Override
        public InputStream getResourceAsStream(String name) {
            return DESCRIPTOR.equals(name) ? new ByteArrayInputStream(descriptor) : super.getResourceAsStream(name);
        }
    }
}
