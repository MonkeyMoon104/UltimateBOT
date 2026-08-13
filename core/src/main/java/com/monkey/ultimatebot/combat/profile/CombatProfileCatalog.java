package com.monkey.ultimatebot.combat.profile;

import java.util.stream.Collectors;


import java.util.Collections;
import com.monkey.ultimatebot.common.model.CombatMode;
import com.monkey.ultimatebot.common.model.CombatTuning;
import com.monkey.ultimatebot.common.model.DifficultyTier;
import com.monkey.ultimatebot.common.model.PlatformCapability;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class CombatProfileCatalog {
    private final Map<CombatMode, CombatModeConfiguration> configurations;
    private volatile Set<PlatformCapability> platformCapabilities;

    public CombatProfileCatalog(Map<CombatMode, CombatModeConfiguration> configurations) {
        this(configurations, EnumSet.allOf(PlatformCapability.class));
    }

    public CombatProfileCatalog(
            Map<CombatMode, CombatModeConfiguration> configurations, Set<PlatformCapability> platformCapabilities) {
        Objects.requireNonNull(configurations, "configurations");
        Map<CombatMode, CombatModeConfiguration> copy = new LinkedHashMap<>();
        copy.putAll(configurations);
        for (CombatMode mode : CombatMode.values()) {
            if (!copy.containsKey(mode)) {
                throw new IllegalArgumentException("Missing combat mode configuration for " + mode);
            }
        }
        this.configurations = com.monkey.ultimatebot.common.util.ImmutableCollections.copyOf(copy);
        bindPlatformCapabilities(platformCapabilities);
    }

    /** Updates the platform capability set used to filter {@link #enabledModes()}. */
    public void bindPlatformCapabilities(Set<PlatformCapability> platformCapabilities) {
        this.platformCapabilities =
                com.monkey.ultimatebot.common.util.ImmutableCollections.copyOf(Objects.requireNonNull(platformCapabilities, "platformCapabilities"));
    }

    public Set<PlatformCapability> platformCapabilities() {
        return platformCapabilities;
    }

    public CombatTuning resolve(CombatMode mode, DifficultyTier difficulty) {
        return configuration(mode).profile(difficulty);
    }

    public CombatModeConfiguration configuration(CombatMode mode) {
        CombatModeConfiguration configuration = configurations.get(Objects.requireNonNull(mode, "mode"));
        if (configuration == null) {
            throw new IllegalStateException("Combat mode is not registered: " + mode);
        }
        return configuration;
    }

    /** Returns whether the loaded platform supports the built-in requirements for {@code mode}. */
    public boolean supports(CombatMode mode) {
        return Objects.requireNonNull(mode, "mode").supportedBy(platformCapabilities);
    }

    /**
     * Modes enabled in config and supported by the loaded platform.
     *
     * <p>GUI cycling, API listing filters, and {@code nextCombatMode} use this intersection.
     */
    public List<CombatMode> enabledModes() {
        return configurations.values().stream()
                .filter(CombatModeConfiguration::enabled)
                .map(CombatModeConfiguration::mode)
                .filter(this::supports)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Modes enabled in config but unavailable because the platform lacks required capabilities.
     */
    public List<CombatMode> platformDisabledModes() {
        return configurations.values().stream()
                .filter(CombatModeConfiguration::enabled)
                .map(CombatModeConfiguration::mode)
                .filter(mode -> !supports(mode))
                .sorted()
                .collect(Collectors.toList());
    }
}
