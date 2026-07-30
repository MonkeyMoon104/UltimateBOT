package com.monkey.mcbot.common.addon;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLClassLoader;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Logger;

/** Downloads, validates and loads optional addons without platform dependencies. */
public final class AddonLoader {
    public static final String ADDON_DIRECTORY = "addon";

    private final Path addonDirectory;
    private final ClassLoader parentClassLoader;
    private final Logger logger;
    private final AddonDownloader downloader;

    public AddonLoader(Path pluginDataDirectory, ClassLoader parentClassLoader, Logger logger) {
        this(
                pluginDataDirectory,
                parentClassLoader,
                logger,
                new HttpAddonDownloader(HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(4))
                        .followRedirects(HttpClient.Redirect.NORMAL)
                        .build()));
    }

    AddonLoader(Path pluginDataDirectory, ClassLoader parentClassLoader, Logger logger, AddonDownloader downloader) {
        this.addonDirectory = Objects.requireNonNull(pluginDataDirectory, "pluginDataDirectory")
                .resolve(ADDON_DIRECTORY);
        this.parentClassLoader = Objects.requireNonNull(parentClassLoader, "parentClassLoader");
        this.logger = Objects.requireNonNull(logger, "logger");
        this.downloader = Objects.requireNonNull(downloader, "downloader");
    }

    public <F, A extends AutoCloseable> LoadedAddon<A> load(
            AddonDefinition definition, Class<F> factoryType, AddonInitializer<F, A> initializer) throws Exception {
        Objects.requireNonNull(definition, "definition");
        Objects.requireNonNull(factoryType, "factoryType");
        Objects.requireNonNull(initializer, "initializer");

        Descriptor descriptor = readDescriptor(definition);
        Path addonJar = addonDirectory.resolve(definition.fileName());
        if (!isValid(addonJar, descriptor)) {
            download(definition, descriptor, addonJar);
        }

        URLClassLoader classLoader =
                new URLClassLoader(new java.net.URL[] {addonJar.toUri().toURL()}, parentClassLoader);
        try {
            Class<?> implementation = Class.forName(descriptor.factoryClass(), true, classLoader);
            Object candidate = implementation.getConstructor().newInstance();
            F factory = factoryType.cast(candidate);
            A addon = Objects.requireNonNull(initializer.initialize(factory), definition.id() + " addon");
            return new LoadedAddon<>(addon, classLoader, logger, definition.displayName());
        } catch (Exception | LinkageError error) {
            try {
                classLoader.close();
            } catch (IOException closeError) {
                error.addSuppressed(closeError);
            }
            throw error;
        }
    }

    private Descriptor readDescriptor(AddonDefinition definition) throws IOException {
        Properties properties = new Properties();
        try (InputStream stream = parentClassLoader.getResourceAsStream(definition.descriptorResource())) {
            if (stream == null) {
                throw new IOException("Embedded " + definition.displayName() + " descriptor is missing");
            }
            properties.load(stream);
        }

        String version = required(properties, "version", definition);
        String overrideUrl = System.getProperty(definition.overrideUrlProperty());
        URI downloadUri;
        try {
            downloadUri = URI.create(overrideUrl == null ? required(properties, "url", definition) : overrideUrl);
        } catch (IllegalArgumentException error) {
            throw new IOException(definition.displayName() + " URL is invalid", error);
        }
        if (overrideUrl == null && !"https".equalsIgnoreCase(downloadUri.getScheme())) {
            throw new IOException(definition.displayName() + " URL must use HTTPS");
        }

        String sha256 = required(properties, "sha256", definition).toLowerCase(Locale.ROOT);
        if (sha256.length() != 64 || !sha256.chars().allMatch(AddonLoader::isHexDigit)) {
            throw new IOException(definition.displayName() + " SHA-256 is invalid");
        }

        long size;
        try {
            size = Long.parseLong(required(properties, "size", definition));
        } catch (NumberFormatException error) {
            throw new IOException(definition.displayName() + " size is invalid", error);
        }
        if (size <= 0L) {
            throw new IOException(definition.displayName() + " size must be positive");
        }
        return new Descriptor(version, downloadUri, sha256, size, required(properties, "factory-class", definition));
    }

    private void download(AddonDefinition definition, Descriptor descriptor, Path addonJar) throws IOException {
        Files.createDirectories(addonDirectory);
        Path temporaryJar = Files.createTempFile(addonDirectory, definition.id() + '-', ".tmp");
        logger.info("Downloading optional " + definition.displayName() + ' ' + descriptor.version() + "...");
        try {
            downloader.download(descriptor.downloadUri(), temporaryJar, descriptor.version());
            if (!isValid(temporaryJar, descriptor)) {
                throw new IOException("Downloaded " + definition.displayName() + " failed integrity validation");
            }
            try {
                Files.move(temporaryJar, addonJar, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporaryJar, addonJar, StandardCopyOption.REPLACE_EXISTING);
            }
            logger.info(definition.displayName() + " installed at " + addonJar);
        } finally {
            Files.deleteIfExists(temporaryJar);
        }
    }

    private static boolean isValid(Path addonJar, Descriptor descriptor) throws IOException {
        return Files.isRegularFile(addonJar)
                && Files.size(addonJar) == descriptor.size()
                && descriptor.sha256().equals(sha256(addonJar));
    }

    private static String sha256(Path file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream stream = Files.newInputStream(file)) {
                byte[] buffer = new byte[16_384];
                int read;
                while ((read = stream.read(buffer)) >= 0) {
                    if (read > 0) {
                        digest.update(buffer, 0, read);
                    }
                }
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException("SHA-256 is unavailable", error);
        }
    }

    private static String required(Properties properties, String key, AddonDefinition definition) throws IOException {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IOException(definition.displayName() + " descriptor is missing " + key);
        }
        return value.trim();
    }

    private static boolean isHexDigit(int character) {
        return (character >= '0' && character <= '9')
                || (character >= 'a' && character <= 'f')
                || (character >= 'A' && character <= 'F');
    }

    private record Descriptor(String version, URI downloadUri, String sha256, long size, String factoryClass) {}

    @FunctionalInterface
    interface AddonDownloader {
        void download(URI source, Path destination, String version) throws IOException;
    }

    private record HttpAddonDownloader(HttpClient httpClient) implements AddonDownloader {
        @Override
        public void download(URI source, Path destination, String version) throws IOException {
            HttpRequest request = HttpRequest.newBuilder(source)
                    .timeout(Duration.ofSeconds(10))
                    .header("Accept", "application/java-archive, application/octet-stream")
                    .header("User-Agent", "MinecraftBot/" + version)
                    .GET()
                    .build();
            try {
                HttpResponse<Path> response = httpClient.send(request, HttpResponse.BodyHandlers.ofFile(destination));
                if (response.statusCode() != 200) {
                    throw new IOException("addon download returned HTTP " + response.statusCode() + " from " + source);
                }
            } catch (InterruptedException error) {
                Thread.currentThread().interrupt();
                throw new IOException("addon download was interrupted", error);
            }
        }
    }
}
