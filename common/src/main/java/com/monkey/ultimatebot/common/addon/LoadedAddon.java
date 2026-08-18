package com.monkey.ultimatebot.common.addon;

import java.io.IOException;
import java.net.URLClassLoader;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class LoadedAddon<A extends AutoCloseable> implements AutoCloseable {
    private final A instance;
    private final URLClassLoader classLoader;
    private final Logger logger;
    private final String displayName;

    LoadedAddon(A instance, URLClassLoader classLoader, Logger logger, String displayName) {
        this.instance = Objects.requireNonNull(instance, "instance");
        this.classLoader = Objects.requireNonNull(classLoader, "classLoader");
        this.logger = Objects.requireNonNull(logger, "logger");
        this.displayName = Objects.requireNonNull(displayName, "displayName");
    }

    public A instance() {
        return instance;
    }

    @Override
    public void close() {
        try {
            instance.close();
        } catch (Exception error) {
            logger.log(Level.WARNING, "Unable to stop " + displayName, error);
        } finally {
            try {
                classLoader.close();
            } catch (IOException error) {
                logger.log(Level.FINE, "Unable to close the " + displayName + " class loader", error);
            }
        }
    }
}
