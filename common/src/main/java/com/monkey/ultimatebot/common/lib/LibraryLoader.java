package com.monkey.ultimatebot.common.lib;

import com.monkey.ultimatebot.common.lib.classpath.LibraryClasspathInjector;
import com.monkey.ultimatebot.common.lib.download.ArtifactDigests;
import com.monkey.ultimatebot.common.lib.download.HttpArtifactDownloader;
import com.monkey.ultimatebot.common.lib.download.LibraryDownloader;
import com.monkey.ultimatebot.common.lib.model.LibraryArtifact;
import com.monkey.ultimatebot.common.lib.model.LibraryDefinition;
import com.monkey.ultimatebot.common.lib.model.LibraryDescriptorReader;
import com.monkey.ultimatebot.common.lib.relocate.LibraryArtifactRelocator;
import com.monkey.ultimatebot.common.lib.relocate.LibraryRelocationRules;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Logger;
import org.jspecify.annotations.Nullable;

public final class LibraryLoader {
    public static final String LIBS_DIRECTORY = "libs";
    public static final String OFFLINE_PROPERTY = "ultimatebot.libs.offline";

    private final Path pluginDataDirectory;
    private final ClassLoader pluginClassLoader;
    private final Logger logger;
    private final LibraryDownloader downloader;
    private final LibraryClasspathInjector injector;
    private final LibraryDescriptorReader descriptorReader;
    private final LibraryArtifactRelocator relocator;
    private final boolean offlineOnly;

    public LibraryLoader(Path pluginDataDirectory, ClassLoader pluginClassLoader, Logger logger) {
        this(
                pluginDataDirectory,
                pluginClassLoader,
                logger,
                new HttpArtifactDownloader(),
                new LibraryClasspathInjector(pluginClassLoader),
                new LibraryDescriptorReader(pluginClassLoader),
                new LibraryArtifactRelocator(),
                Boolean.getBoolean(OFFLINE_PROPERTY));
    }

    LibraryLoader(
            Path pluginDataDirectory,
            ClassLoader pluginClassLoader,
            Logger logger,
            LibraryDownloader downloader,
            LibraryClasspathInjector injector,
            LibraryDescriptorReader descriptorReader,
            LibraryArtifactRelocator relocator,
            boolean offlineOnly) {
        this.pluginDataDirectory = Objects.requireNonNull(pluginDataDirectory, "pluginDataDirectory");
        this.pluginClassLoader = Objects.requireNonNull(pluginClassLoader, "pluginClassLoader");
        this.logger = Objects.requireNonNull(logger, "logger");
        this.downloader = Objects.requireNonNull(downloader, "downloader");
        this.injector = Objects.requireNonNull(injector, "injector");
        this.descriptorReader = Objects.requireNonNull(descriptorReader, "descriptorReader");
        this.relocator = Objects.requireNonNull(relocator, "relocator");
        this.offlineOnly = offlineOnly;
    }

    public List<LibraryLoadResult> loadAll(List<LibraryRequest> requests) throws Exception {
        Objects.requireNonNull(requests, "requests");
        List<LibraryLoadResult> results = new ArrayList<LibraryLoadResult>(requests.size());
        for (LibraryRequest request : requests) {
            results.add(load(request));
        }
        return results;
    }

    public LibraryLoadResult load(LibraryRequest request) throws Exception {
        Objects.requireNonNull(request, "request");
        LibraryDefinition definition = descriptorReader.read(
                request.id(),
                request.displayName(),
                request.descriptorResource(),
                request.overrideUrlPropertyPrefix(),
                request.trackOverride());

        if (isKeyClassPresent(definition.keyClass())) {
            logger.info("[Libs] " + definition.displayName() + " -> reuse classpath (" + definition.keyClass() + ")");
            return LibraryLoadResult.reused(definition);
        }

        Path trackDirectory = pluginDataDirectory
                .resolve(LIBS_DIRECTORY)
                .resolve(definition.track().folderName());
        Files.createDirectories(trackDirectory);

        List<Path> jarPaths = new ArrayList<Path>(definition.artifacts().size());
        PrepareMode mode = PrepareMode.CACHED;
        for (int index = 0; index < definition.artifacts().size(); index++) {
            LibraryArtifact artifact = definition.artifacts().get(index);
            Path relocatedJar = trackDirectory.resolve(relocatedFileName(artifact.fileName()));
            Path metaFile = metaFile(relocatedJar);
            Path originalJar = trackDirectory.resolve(artifact.fileName());
            if (!isRelocatedCacheValid(relocatedJar, metaFile, artifact)) {
                PrepareMode artifactMode =
                        prepareRelocated(definition, artifact, originalJar, relocatedJar, metaFile, index);
                if (artifactMode.ordinal() > mode.ordinal()) {
                    mode = artifactMode;
                }
            }
            jarPaths.add(relocatedJar);
        }

        for (Path jarPath : jarPaths) {
            injector.addJar(jarPath);
        }

        if (!isKeyClassPresent(definition.keyClass())) {
            throw new IllegalStateException(
                    "Library "
                            + definition.id()
                            + " injected but key class still missing: "
                            + definition.keyClass());
        }

        logger.info(
                "[Libs] "
                        + definition.displayName()
                        + " -> "
                        + mode.logLabel()
                        + " under libs/"
                        + definition.track().folderName()
                        + " ("
                        + jarPaths.size()
                        + " jar(s))");
        return mode.toResult(definition);
    }

    private PrepareMode prepareRelocated(
            LibraryDefinition definition,
            LibraryArtifact artifact,
            Path originalJar,
            Path relocatedJar,
            Path metaFile,
            int index)
            throws IOException {
        if (isOriginalValid(originalJar, artifact)) {
            logger.info("[Libs] Relocating local " + artifact.fileName() + " (no download)");
            relocateTo(originalJar, relocatedJar, metaFile, artifact.sha256());
            return PrepareMode.LOCAL;
        }

        if (offlineOnly) {
            throw new IOException(
                    "Offline libs mode: missing local artifact "
                            + artifact.fileName()
                            + " under libs/"
                            + definition.track().folderName()
                            + " (expected SHA-256 "
                            + artifact.sha256()
                            + ")");
        }

        Path temporaryOriginal =
                Files.createTempFile(relocatedJar.getParent(), definition.id() + '-' + index + "-orig-", ".tmp");
        try {
            logger.info("[Libs] Downloading " + definition.displayName() + " / " + artifact.fileName() + "...");
            downloadWithFallback(definition, artifact, temporaryOriginal);
            if (!isOriginalValid(temporaryOriginal, artifact)) {
                throw new IOException(
                        "Downloaded "
                                + definition.id()
                                + " artifact failed integrity validation: "
                                + artifact.fileName());
            }
            atomicMove(temporaryOriginal, originalJar);
            logger.info("[Libs] Relocating " + artifact.fileName() + " -> " + LibraryRelocationRules.ROOT + ".*");
            relocateTo(originalJar, relocatedJar, metaFile, artifact.sha256());
            return PrepareMode.DOWNLOADED;
        } finally {
            Files.deleteIfExists(temporaryOriginal);
        }
    }

    private void relocateTo(Path originalJar, Path relocatedJar, Path metaFile, String originalSha256)
            throws IOException {
        Path temporaryRelocated =
                relocatedJar.resolveSibling(relocatedJar.getFileName().toString() + "." + System.nanoTime() + ".tmp");
        Files.deleteIfExists(temporaryRelocated);
        try {
            relocator.relocate(originalJar, temporaryRelocated);
            atomicMove(temporaryRelocated, relocatedJar);
            writeMeta(metaFile, originalSha256);
        } finally {
            Files.deleteIfExists(temporaryRelocated);
        }
    }

    private void downloadWithFallback(LibraryDefinition definition, LibraryArtifact artifact, Path destination)
            throws IOException {
        IOException lastFailure = null;
        List<URI> uris = artifact.downloadUris();
        for (int i = 0; i < uris.size(); i++) {
            URI uri = uris.get(i);
            try {
                Files.deleteIfExists(destination);
                downloader.download(uri, destination, "UltimateBot-LibraryLoader/" + definition.id());
                if (isOriginalValid(destination, artifact)) {
                    if (i > 0) {
                        logger.info(
                                "[Libs] "
                                        + artifact.fileName()
                                        + " fetched via fallback repo #"
                                        + (i + 1));
                    }
                    return;
                }
                lastFailure = new IOException(
                        "integrity check failed for " + artifact.fileName() + " from " + uri);
            } catch (IOException error) {
                lastFailure = error;
                logger.warning(
                        "[Libs] download failed for "
                                + artifact.fileName()
                                + " from "
                                + uri
                                + ": "
                                + error.getMessage());
            }
        }
        throw lastFailure != null
                ? new IOException(
                        "Unable to download "
                                + artifact.fileName()
                                + " from any configured repository. "
                                + "Place the original jar under plugins/UltimateBot/libs/<track>/ "
                                + "or restore network access.",
                        lastFailure)
                : new IOException("No download URLs for " + artifact.fileName());
    }

    private static void atomicMove(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private boolean isKeyClassPresent(String keyClass) {
        try {
            Class.forName(keyClass, false, pluginClassLoader);
            return true;
        } catch (ClassNotFoundException | LinkageError ignored) {
            return false;
        }
    }

    private static boolean isOriginalValid(Path jar, LibraryArtifact artifact) throws IOException {
        return Files.isRegularFile(jar)
                && Files.size(jar) == artifact.size()
                && artifact.sha256().equals(ArtifactDigests.sha256(jar));
    }

    private static boolean isRelocatedCacheValid(Path relocatedJar, Path metaFile, LibraryArtifact artifact)
            throws IOException {
        if (!Files.isRegularFile(relocatedJar) || !Files.isRegularFile(metaFile)) {
            return false;
        }
        String meta = new String(Files.readAllBytes(metaFile), StandardCharsets.UTF_8);
        return meta.contains("original-sha256=" + artifact.sha256().toLowerCase(Locale.ROOT))
                && meta.contains("rules-version=" + LibraryRelocationRules.RULES_VERSION);
    }

    private static void writeMeta(Path metaFile, String originalSha256) throws IOException {
        String content = "original-sha256="
                + originalSha256.toLowerCase(Locale.ROOT)
                + "\nrules-version="
                + LibraryRelocationRules.RULES_VERSION
                + "\n";
        Files.write(metaFile, content.getBytes(StandardCharsets.UTF_8));
    }

    private static Path metaFile(Path relocatedJar) {
        return relocatedJar.resolveSibling(relocatedJar.getFileName().toString() + ".meta");
    }

    private static String relocatedFileName(String originalFileName) {
        if (originalFileName.endsWith(".jar")) {
            return originalFileName.substring(0, originalFileName.length() - 4) + "-relocated.jar";
        }
        return originalFileName + "-relocated.jar";
    }

    private enum PrepareMode {
        CACHED("cached"),
        LOCAL("local+relocated"),
        DOWNLOADED("downloaded+relocated");

        private final String logLabel;

        PrepareMode(String logLabel) {
            this.logLabel = logLabel;
        }

        String logLabel() {
            return logLabel;
        }

        LibraryLoadResult toResult(LibraryDefinition definition) {
            if (this == DOWNLOADED) {
                return LibraryLoadResult.downloaded(definition);
            }
            return LibraryLoadResult.cached(definition);
        }
    }

    public static final class LibraryRequest {
        private final String id;
        private final String displayName;
        private final String descriptorResource;
        private final String overrideUrlPropertyPrefix;
        private final @Nullable LibraryTrack trackOverride;

        public LibraryRequest(
                String id, String displayName, String descriptorResource, String overrideUrlPropertyPrefix) {
            this(id, displayName, descriptorResource, overrideUrlPropertyPrefix, null);
        }

        public LibraryRequest(
                String id,
                String displayName,
                String descriptorResource,
                String overrideUrlPropertyPrefix,
                @Nullable LibraryTrack trackOverride) {
            this.id = Objects.requireNonNull(id, "id");
            this.displayName = Objects.requireNonNull(displayName, "displayName");
            this.descriptorResource = Objects.requireNonNull(descriptorResource, "descriptorResource");
            this.overrideUrlPropertyPrefix =
                    Objects.requireNonNull(overrideUrlPropertyPrefix, "overrideUrlPropertyPrefix");
            this.trackOverride = trackOverride;
        }

        public String id() {
            return id;
        }

        public String displayName() {
            return displayName;
        }

        public String descriptorResource() {
            return descriptorResource;
        }

        public String overrideUrlPropertyPrefix() {
            return overrideUrlPropertyPrefix;
        }

        public @Nullable LibraryTrack trackOverride() {
            return trackOverride;
        }
    }

    public static final class LibraryLoadResult {
        public enum Source {
            REUSED,
            CACHED,
            DOWNLOADED
        }

        private final LibraryDefinition definition;
        private final Source source;

        private LibraryLoadResult(LibraryDefinition definition, Source source) {
            this.definition = definition;
            this.source = source;
        }

        public static LibraryLoadResult reused(LibraryDefinition definition) {
            return new LibraryLoadResult(definition, Source.REUSED);
        }

        public static LibraryLoadResult cached(LibraryDefinition definition) {
            return new LibraryLoadResult(definition, Source.CACHED);
        }

        public static LibraryLoadResult downloaded(LibraryDefinition definition) {
            return new LibraryLoadResult(definition, Source.DOWNLOADED);
        }

        public LibraryDefinition definition() {
            return definition;
        }

        public Source source() {
            return source;
        }
    }
}
