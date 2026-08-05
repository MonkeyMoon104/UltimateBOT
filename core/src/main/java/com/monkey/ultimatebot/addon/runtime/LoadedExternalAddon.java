package com.monkey.ultimatebot.addon.runtime;

import com.monkey.ultimatebot.api.addon.AddonDescriptor;
import com.monkey.ultimatebot.api.addon.UltimateBotAddon;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

final class LoadedExternalAddon implements AutoCloseable {
    private final AddonDescriptor descriptor;
    private final Path jar;
    private final UltimateBotAddon addon;
    private final CoreAddonResourceScope resources;
    private final AddonClassLoader classLoader;
    private final Logger logger;
    private boolean closed;

    LoadedExternalAddon(
            AddonDescriptor descriptor,
            Path jar,
            UltimateBotAddon addon,
            CoreAddonResourceScope resources,
            AddonClassLoader classLoader,
            Logger logger) {
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
        this.jar = Objects.requireNonNull(jar, "jar");
        this.addon = Objects.requireNonNull(addon, "addon");
        this.resources = Objects.requireNonNull(resources, "resources");
        this.classLoader = Objects.requireNonNull(classLoader, "classLoader");
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    AddonDescriptor descriptor() {
        return descriptor;
    }

    Path jar() {
        return jar;
    }

    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }
        closed = true;
        try {
            addon.onDisable();
        } catch (Exception | LinkageError error) {
            logger.log(Level.WARNING, "Addon " + descriptor.id() + " failed while disabling", error);
        } finally {
            resources.close();
            try {
                classLoader.close();
            } catch (IOException error) {
                logger.log(Level.WARNING, "Unable to close addon " + descriptor.id() + " classloader", error);
            }
        }
    }
}
