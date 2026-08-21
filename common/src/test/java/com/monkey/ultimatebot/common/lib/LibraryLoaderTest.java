package com.monkey.ultimatebot.common.lib;

import static org.assertj.core.api.Assertions.assertThat;

import com.monkey.ultimatebot.common.lib.classpath.LibraryClasspathInjector;
import com.monkey.ultimatebot.common.lib.download.ArtifactDigests;
import com.monkey.ultimatebot.common.lib.download.LibraryDownloader;
import com.monkey.ultimatebot.common.lib.model.LibraryDescriptorReader;
import com.monkey.ultimatebot.common.lib.relocate.LibraryArtifactRelocator;
import com.monkey.ultimatebot.common.lib.relocate.LibraryRelocationRules;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LibraryLoaderTest {
    private static final byte[] JAR_BYTES = "verified-library-jar".getBytes(StandardCharsets.UTF_8);
    private static final String MISSING_KEY = "com.monkey.ultimatebot.libs.jackson.databind.ObjectMapper";

    @TempDir
    Path temporaryDirectory;

    @Test
    void reusesWhenKeyClassAlreadyPresent() throws Exception {
        AtomicInteger downloads = new AtomicInteger();
        LibraryDownloader downloader = (source, destination, userAgent) -> downloads.incrementAndGet();
        String descriptorPath = "test/lib-reuse.properties";
        ClassLoader resources =
                new DescriptorClassLoader(getClass().getClassLoader(), descriptorPath, descriptor("java.lang.String"));
        LibraryLoader loader = new LibraryLoader(
                temporaryDirectory,
                getClass().getClassLoader(),
                Logger.getLogger(LibraryLoaderTest.class.getName()),
                downloader,
                new LibraryClasspathInjector(new URLClassLoader(new URL[0], getClass().getClassLoader())),
                new LibraryDescriptorReader(resources),
                new LibraryArtifactRelocator(),
                false);

        LibraryLoader.LibraryLoadResult result = loader.load(new LibraryLoader.LibraryRequest(
                "demo", "Demo", descriptorPath, "ultimatebot.lib.demo", LibraryTrack.LEGACY));

        assertThat(result.source()).isEqualTo(LibraryLoader.LibraryLoadResult.Source.REUSED);
        assertThat(downloads).hasValue(0);
    }

    @Test
    void downloadsAndCachesWhenKeyClassMissing() throws Exception {
        AtomicInteger downloads = new AtomicInteger();
        LibraryDownloader downloader = (source, destination, userAgent) -> {
            downloads.incrementAndGet();
            Files.write(destination, JAR_BYTES);
        };
        LibraryArtifactRelocator relocator = new LibraryArtifactRelocator() {
            @Override
            public void relocate(Path inputJar, Path outputJar) throws java.io.IOException {
                Files.createDirectories(outputJar.getParent());
                Files.copy(inputJar, outputJar, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        };
        URLClassLoader injectTarget = new URLClassLoader(new URL[0], getClass().getClassLoader());
        String descriptorPath = "test/lib-download.properties";
        ClassLoader resources =
                new DescriptorClassLoader(getClass().getClassLoader(), descriptorPath, descriptor(MISSING_KEY));
        LibraryLoader loader = new LibraryLoader(
                temporaryDirectory,
                injectTarget,
                Logger.getLogger(LibraryLoaderTest.class.getName()),
                downloader,
                new LibraryClasspathInjector(injectTarget),
                new LibraryDescriptorReader(resources),
                relocator,
                false);

        try {
            loader.load(new LibraryLoader.LibraryRequest(
                    "demo", "Demo", descriptorPath, "ultimatebot.lib.demo", LibraryTrack.LEGACY));
        } catch (Exception | LinkageError ignored) {
            // Stub jar cannot provide the relocated key class.
        }

        assertThat(downloads).hasValue(1);
        assertThat(temporaryDirectory.resolve("libs/legacy/demo-lib-relocated.jar")).hasBinaryContent(JAR_BYTES);
        assertThat(temporaryDirectory.resolve("libs/legacy/demo-lib-relocated.jar.meta").toFile())
                .exists();
        String meta = new String(
                Files.readAllBytes(temporaryDirectory.resolve("libs/legacy/demo-lib-relocated.jar.meta")),
                StandardCharsets.UTF_8);
        assertThat(meta).contains("rules-version=" + LibraryRelocationRules.RULES_VERSION);
        injectTarget.close();
    }

    @Test
    void relocateClassNameMapsKnownPackages() {
        assertThat(LibraryRelocationRules.relocateClassName("com.fasterxml.jackson.databind.ObjectMapper"))
                .isEqualTo("com.monkey.ultimatebot.libs.jackson.databind.ObjectMapper");
        assertThat(LibraryRelocationRules.relocateClassName("xyz.xenondevs.invui.gui.Gui"))
                .isEqualTo("com.monkey.ultimatebot.libs.invui.gui.Gui");
    }

    private static byte[] descriptor(String keyClass) throws Exception {
        String sha256 = ArtifactDigests.toHex(MessageDigest.getInstance("SHA-256").digest(JAR_BYTES));
        return ("track=legacy\n"
                        + "key-class="
                        + keyClass
                        + "\njar-count=1\n"
                        + "jar.0.file=demo-lib.jar\n"
                        + "jar.0.url-count=2\n"
                        + "jar.0.url.0=https://example.invalid/demo-lib.jar\n"
                        + "jar.0.url.1=https://example.invalid/mirror/demo-lib.jar\n"
                        + "jar.0.sha256="
                        + sha256
                        + "\njar.0.size="
                        + JAR_BYTES.length
                        + "\n")
                .getBytes(StandardCharsets.UTF_8);
    }

    private static final class DescriptorClassLoader extends ClassLoader {
        private final String resource;
        private final byte[] bytes;

        private DescriptorClassLoader(ClassLoader parent, String resource, byte[] bytes) {
            super(parent);
            this.resource = resource;
            this.bytes = bytes;
        }

        @Override
        public java.io.InputStream getResourceAsStream(String name) {
            if (resource.equals(name)) {
                return new java.io.ByteArrayInputStream(bytes);
            }
            return super.getResourceAsStream(name);
        }
    }
}
