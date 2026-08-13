package com.monkey.ultimatebot.common.addon;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Logger;
import com.monkey.ultimatebot.common.util.TextValues;

/** Downloads, validates and loads optional addons without platform dependencies. */
public final class AddonLoader {
    public static final String ADDON_DIRECTORY = "addon";
    private static final int CONNECT_TIMEOUT_MS = 4_000;
    private static final int READ_TIMEOUT_MS = 10_000;
    private static final int MAX_REDIRECTS = 5;

    private final Path addonDirectory;
    private final ClassLoader parentClassLoader;
    private final Logger logger;
    private final AddonDownloader downloader;

    public AddonLoader(Path pluginDataDirectory, ClassLoader parentClassLoader, Logger logger) {
        this(pluginDataDirectory, parentClassLoader, logger, new HttpAddonDownloader());
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
            Class<?> implementation = Class.forName(descriptor.factoryClass, true, classLoader);
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
        if (sha256.length() != 64 || !allHexDigits(sha256)) {
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
        logger.info("Downloading optional " + definition.displayName() + ' ' + descriptor.version + "...");
        try {
            downloader.download(descriptor.downloadUri, temporaryJar, descriptor.version);
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
                && Files.size(addonJar) == descriptor.size
                && descriptor.sha256.equals(sha256(addonJar));
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
            return toHex(digest.digest());
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException("SHA-256 is unavailable", error);
        }
    }

    private static String toHex(byte[] bytes) {
        char[] hex = new char[bytes.length * 2];
        for (int index = 0; index < bytes.length; index++) {
            int value = bytes[index] & 0xFF;
            hex[index * 2] = Character.forDigit(value >>> 4, 16);
            hex[index * 2 + 1] = Character.forDigit(value & 0x0F, 16);
        }
        return new String(hex);
    }

    private static String required(Properties properties, String key, AddonDefinition definition) throws IOException {
        String value = properties.getProperty(key);
        if (TextValues.isBlank(value)) {
            throw new IOException(definition.displayName() + " descriptor is missing " + key);
        }
        return value.trim();
    }

    private static boolean allHexDigits(String value) {
        for (int index = 0; index < value.length(); index++) {
            if (!isHexDigit(value.charAt(index))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isHexDigit(int character) {
        return (character >= '0' && character <= '9')
                || (character >= 'a' && character <= 'f')
                || (character >= 'A' && character <= 'F');
    }

    private static final class Descriptor {
        private final String version;
        private final URI downloadUri;
        private final String sha256;
        private final long size;
        private final String factoryClass;

        private Descriptor(String version, URI downloadUri, String sha256, long size, String factoryClass) {
            this.version = version;
            this.downloadUri = downloadUri;
            this.sha256 = sha256;
            this.size = size;
            this.factoryClass = factoryClass;
        }
    }

    @FunctionalInterface
    interface AddonDownloader {
        void download(URI source, Path destination, String version) throws IOException;
    }

    private static final class HttpAddonDownloader implements AddonDownloader {
        @Override
        public void download(URI source, Path destination, String version) throws IOException {
            URL current = source.toURL();
            for (int redirect = 0; redirect <= MAX_REDIRECTS; redirect++) {
                HttpURLConnection connection = (HttpURLConnection) current.openConnection();
                connection.setInstanceFollowRedirects(false);
                connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
                connection.setReadTimeout(READ_TIMEOUT_MS);
                connection.setRequestProperty("Accept", "application/java-archive, application/octet-stream");
                connection.setRequestProperty("User-Agent", "UltimateBot/" + version);
                connection.setRequestMethod("GET");
                int status = connection.getResponseCode();
                if (status == HttpURLConnection.HTTP_MOVED_PERM
                        || status == HttpURLConnection.HTTP_MOVED_TEMP
                        || status == HttpURLConnection.HTTP_SEE_OTHER
                        || status == 307
                        || status == 308) {
                    String location = connection.getHeaderField("Location");
                    connection.disconnect();
                    if (location == null || location.isEmpty()) {
                        throw new IOException("addon download redirect missing Location from " + current);
                    }
                    current = new URL(current, location);
                    continue;
                }
                if (status != HttpURLConnection.HTTP_OK) {
                    connection.disconnect();
                    throw new IOException("addon download returned HTTP " + status + " from " + source);
                }
                try (InputStream input = connection.getInputStream();
                        OutputStream output = Files.newOutputStream(destination)) {
                    byte[] buffer = new byte[16_384];
                    int read;
                    while ((read = input.read(buffer)) >= 0) {
                        if (read > 0) {
                            output.write(buffer, 0, read);
                        }
                    }
                } finally {
                    connection.disconnect();
                }
                return;
            }
            throw new IOException("addon download exceeded redirect limit from " + source);
        }
    }
}
