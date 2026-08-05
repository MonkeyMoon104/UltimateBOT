package com.monkey.ultimatebot.addon.runtime;

import com.monkey.ultimatebot.api.UltimateBotAPI;
import com.monkey.ultimatebot.api.addon.AddonContext;
import com.monkey.ultimatebot.api.addon.AddonResourceScope;
import com.monkey.ultimatebot.api.extension.ExtensionRegistration;
import com.monkey.ultimatebot.api.extension.brain.BotBrainProvider;
import com.monkey.ultimatebot.api.extension.combat.CombatModeProvider;
import com.monkey.ultimatebot.extension.registry.CoreExtensionRegistry;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Logger;

final class CoreAddonContext implements AddonContext {
    private final String addonId;
    private final UltimateBotAPI api;
    private final Path dataDirectory;
    private final Logger logger;
    private final CoreAddonResourceScope resources;
    private final CoreExtensionRegistry extensions;

    CoreAddonContext(
            String addonId,
            UltimateBotAPI api,
            Path dataDirectory,
            Logger logger,
            CoreAddonResourceScope resources,
            CoreExtensionRegistry extensions) {
        this.addonId = Objects.requireNonNull(addonId, "addonId");
        this.api = Objects.requireNonNull(api, "api");
        this.dataDirectory = Objects.requireNonNull(dataDirectory, "dataDirectory");
        this.logger = Objects.requireNonNull(logger, "logger");
        this.resources = Objects.requireNonNull(resources, "resources");
        this.extensions = Objects.requireNonNull(extensions, "extensions");
    }

    @Override
    public String addonId() {
        return addonId;
    }

    @Override
    public UltimateBotAPI api() {
        return api;
    }

    @Override
    public Path dataDirectory() {
        return dataDirectory;
    }

    @Override
    public Logger logger() {
        return logger;
    }

    @Override
    public AddonResourceScope resources() {
        return resources;
    }

    @Override
    public ExtensionRegistration registerCombatMode(CombatModeProvider provider) {
        return resources.own(extensions.registerCombatMode(addonId, provider));
    }

    @Override
    public ExtensionRegistration registerBrain(BotBrainProvider provider) {
        return resources.own(extensions.registerBrain(addonId, provider));
    }
}
