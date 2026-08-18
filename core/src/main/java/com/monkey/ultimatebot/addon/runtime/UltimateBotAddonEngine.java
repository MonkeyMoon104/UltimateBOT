package com.monkey.ultimatebot.addon.runtime;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.api.UltimateBotAPI;
import com.monkey.ultimatebot.api.addon.AddonDescriptor;
import com.monkey.ultimatebot.api.addon.AddonSnapshot;
import com.monkey.ultimatebot.api.addon.AddonState;
import com.monkey.ultimatebot.api.addon.UltimateBotAddon;
import com.monkey.ultimatebot.api.event.addon.AddonLifecycleEvent;
import com.monkey.ultimatebot.extension.registry.CoreExtensionRegistry;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import org.jspecify.annotations.Nullable;

public final class UltimateBotAddonEngine implements AutoCloseable {
    public static final String ADDON_DIRECTORY = "addon";
    public static final String API_VERSION = "2";

    private final UltimateBot plugin;
    private final UltimateBotAPI api;
    private final CoreExtensionRegistry extensions;
    private final CoreAddonRegistry registry;
    private final Path addonDirectory;
    private final Deque<LoadedExternalAddon> loaded = new ArrayDeque<>();
    private boolean closed;

    public UltimateBotAddonEngine(
            UltimateBot plugin, UltimateBotAPI api, CoreExtensionRegistry extensions, CoreAddonRegistry registry) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.api = Objects.requireNonNull(api, "api");
        this.extensions = Objects.requireNonNull(extensions, "extensions");
        this.registry = Objects.requireNonNull(registry, "registry");
        this.addonDirectory = plugin.getDataFolder().toPath().resolve(ADDON_DIRECTORY);
    }

    public int loadAll() {
        ensureOpen();
        try {
            Files.createDirectories(addonDirectory);
            AddonDiscoveryService discovery =
                    new AddonDiscoveryService(plugin.getLogger(), this::publish, this::failDiscovery);
            Map<String, AddonDependencyResolver.DiscoveredAddon> discovered = discovery.discover(addonDirectory);
            discovery.removeInvalidDependencies(discovered);
            List<AddonDependencyResolver.DiscoveredAddon> ordered;
            try {
                ordered = AddonDependencyResolver.resolve(discovered);
            } catch (AddonLoadException error) {
                discovered
                        .values()
                        .forEach(candidate -> fail(candidate.descriptor(), candidate.jar(), error.getMessage(), error));
                return 0;
            }
            int enabled = 0;
            Set<String> enabledIds = new HashSet<>();
            for (AddonDependencyResolver.DiscoveredAddon candidate : ordered) {
                String failedDependency = candidate.descriptor().dependencies().stream()
                        .filter(dependency -> !enabledIds.contains(dependency))
                        .findFirst()
                        .orElse(null);
                if (failedDependency != null) {
                    fail(
                            candidate.descriptor(),
                            candidate.jar(),
                            "required addon " + failedDependency + " did not enable",
                            null);
                    continue;
                }
                if (load(candidate)) {
                    enabled++;
                    enabledIds.add(candidate.descriptor().id());
                }
            }
            return enabled;
        } catch (IOException error) {
            plugin.getLogger().log(Level.SEVERE, "Unable to initialize the addon engine", error);
            return 0;
        }
    }

    private void failDiscovery(AddonDependencyResolver.DiscoveredAddon candidate, String reason) {
        fail(candidate.descriptor(), candidate.jar(), reason, null);
    }

    private boolean load(AddonDependencyResolver.DiscoveredAddon candidate) {
        AddonDescriptor descriptor = candidate.descriptor();
        Path jar = candidate.jar();
        if (!API_VERSION.equals(descriptor.apiVersion())) {
            fail(descriptor, jar, "unsupported API version " + descriptor.apiVersion(), null);
            return false;
        }
        String entrypoint = descriptor
                .nativeProviders()
                .getOrDefault(
                        com.monkey.ultimatebot.access.runtime.MinecraftVersionAccess.minecraftVersion(),
                        descriptor.mainClass());
        AddonClassLoader classLoader = null;
        UltimateBotAddon addon = null;
        boolean lifecycleStarted = false;
        CoreAddonResourceScope resources = new CoreAddonResourceScope(plugin, plugin.getLogger());
        try {
            URL jarUrl = jar.toUri().toURL();
            classLoader = new AddonClassLoader(jarUrl, plugin.getClass().getClassLoader());
            Class<?> mainClass = Class.forName(entrypoint, true, classLoader);
            addon = UltimateBotAddon.class.cast(
                    mainClass.getDeclaredConstructor().newInstance());
            Path dataDirectory = addonDirectory.resolve(descriptor.id());
            Files.createDirectories(dataDirectory);
            CoreAddonContext context = new CoreAddonContext(
                    descriptor.id(), api, dataDirectory, plugin.getLogger(), resources, extensions);
            lifecycleStarted = true;
            addon.onLoad(context);
            publish(new AddonSnapshot(descriptor, AddonState.LOADED, jar, null));
            addon.onEnable();
            LoadedExternalAddon loadedAddon =
                    new LoadedExternalAddon(descriptor, jar, addon, resources, classLoader, plugin.getLogger());
            loaded.push(loadedAddon);
            publish(new AddonSnapshot(descriptor, AddonState.ENABLED, jar, null));
            plugin.getLogger().info("Enabled addon " + descriptor.name() + " v" + descriptor.version());
            return true;
        } catch (Exception | LinkageError error) {
            if (lifecycleStarted && addon != null) {
                disableFailedAddon(addon, error);
            }
            resources.close();
            closeClassLoader(classLoader, error);
            fail(descriptor, jar, error.getMessage(), error);
            return false;
        }
    }

    private static void disableFailedAddon(UltimateBotAddon addon, Throwable startupFailure) {
        try {
            addon.onDisable();
        } catch (Exception | LinkageError disableFailure) {
            startupFailure.addSuppressed(disableFailure);
        }
    }

    private void fail(AddonDescriptor descriptor, Path jar, @Nullable String message, @Nullable Throwable error) {
        String reason = message == null || message.trim().isEmpty() ? "unknown startup failure" : message;
        publish(new AddonSnapshot(descriptor, AddonState.FAILED, jar, reason));
        plugin.getLogger().warning("Addon " + descriptor.id() + " failed -> " + reason);
        if (error != null) {
            plugin.getLogger().log(Level.FINE, "Addon " + descriptor.id() + " startup failure", error);
        }
    }

    private void closeClassLoader(@Nullable AddonClassLoader classLoader, Throwable original) {
        if (classLoader == null) {
            return;
        }
        try {
            classLoader.close();
        } catch (IOException error) {
            original.addSuppressed(error);
        }
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        while (!loaded.isEmpty()) {
            LoadedExternalAddon addon = loaded.pop();
            addon.close();
            extensions.unregisterOwner(addon.descriptor().id());
            publish(new AddonSnapshot(addon.descriptor(), AddonState.DISABLED, addon.jar(), null));
        }
    }

    private void publish(AddonSnapshot snapshot) {
        registry.update(snapshot);
        plugin.getServer().getPluginManager().callEvent(new AddonLifecycleEvent(snapshot));
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("addon engine is closed");
        }
    }
}
