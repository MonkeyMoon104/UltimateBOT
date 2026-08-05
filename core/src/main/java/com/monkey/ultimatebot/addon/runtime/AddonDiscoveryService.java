package com.monkey.ultimatebot.addon.runtime;

import com.monkey.ultimatebot.api.addon.AddonDescriptor;
import com.monkey.ultimatebot.api.addon.AddonSnapshot;
import com.monkey.ultimatebot.api.addon.AddonState;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Stream;

final class AddonDiscoveryService {
    private final Logger logger;
    private final Consumer<AddonSnapshot> publish;
    private final BiConsumer<AddonDependencyResolver.DiscoveredAddon, String> fail;

    AddonDiscoveryService(
            Logger logger,
            Consumer<AddonSnapshot> publish,
            BiConsumer<AddonDependencyResolver.DiscoveredAddon, String> fail) {
        this.logger = Objects.requireNonNull(logger, "logger");
        this.publish = Objects.requireNonNull(publish, "publish");
        this.fail = Objects.requireNonNull(fail, "fail");
    }

    Map<String, AddonDependencyResolver.DiscoveredAddon> discover(Path directory) throws IOException {
        Map<String, AddonDependencyResolver.DiscoveredAddon> discovered = new LinkedHashMap<>();
        try (Stream<Path> files = Files.list(Objects.requireNonNull(directory, "directory"))) {
            for (Path jar : files.filter(Files::isRegularFile)
                    .filter(AddonDiscoveryService::isJar)
                    .sorted()
                    .toList()) {
                discover(jar, discovered);
            }
        }
        return discovered;
    }

    void removeInvalidDependencies(Map<String, AddonDependencyResolver.DiscoveredAddon> discovered) {
        removeMissingDependencies(discovered);
        for (String addonId : AddonDependencyResolver.cyclicAddons(discovered)) {
            AddonDependencyResolver.DiscoveredAddon candidate = discovered.remove(addonId);
            if (candidate != null) {
                fail.accept(candidate, "cyclic addon dependency");
            }
        }
        removeMissingDependencies(discovered);
    }

    private void discover(Path jar, Map<String, AddonDependencyResolver.DiscoveredAddon> discovered) {
        try {
            AddonDescriptor descriptor = AddonDescriptorParser.parse(jar);
            if (descriptor == null) {
                return;
            }
            AddonDescriptorParser.validateJarContents(jar);
            AddonDependencyResolver.DiscoveredAddon candidate =
                    new AddonDependencyResolver.DiscoveredAddon(descriptor, jar);
            if (discovered.putIfAbsent(descriptor.id(), candidate) != null) {
                throw new AddonLoadException("duplicate addon id " + descriptor.id());
            }
            publish.accept(new AddonSnapshot(descriptor, AddonState.DISCOVERED, jar, null));
        } catch (AddonLoadException error) {
            logger.warning("Skipping addon " + jar.getFileName() + " -> " + error.getMessage());
        }
    }

    private void removeMissingDependencies(Map<String, AddonDependencyResolver.DiscoveredAddon> discovered) {
        boolean removed;
        do {
            removed = false;
            var iterator = discovered.entrySet().iterator();
            while (iterator.hasNext()) {
                AddonDependencyResolver.DiscoveredAddon candidate =
                        iterator.next().getValue();
                String missing = candidate.descriptor().dependencies().stream()
                        .filter(dependency -> !discovered.containsKey(dependency))
                        .findFirst()
                        .orElse(null);
                if (missing != null) {
                    iterator.remove();
                    removed = true;
                    fail.accept(candidate, "missing required addon " + missing);
                }
            }
        } while (removed);
    }

    private static boolean isJar(Path path) {
        return path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".jar");
    }
}
