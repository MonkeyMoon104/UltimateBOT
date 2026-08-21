package com.monkey.ultimatebot.combat.profile;

import com.monkey.ultimatebot.common.model.combat.CombatMode;
import com.monkey.ultimatebot.common.model.combat.CombatTuning;
import com.monkey.ultimatebot.common.model.combat.DifficultyTier;
import com.monkey.ultimatebot.common.model.platform.PlatformCapability;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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

    public void bindPlatformCapabilities(Set<PlatformCapability> platformCapabilities) {
        this.platformCapabilities = com.monkey.ultimatebot.common.util.ImmutableCollections.copyOf(
                Objects.requireNonNull(platformCapabilities, "platformCapabilities"));
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

    public boolean supports(CombatMode mode) {
        return Objects.requireNonNull(mode, "mode").supportedBy(platformCapabilities);
    }

    public List<CombatMode> enabledModes() {
        return configurations.values().stream()
                .filter(CombatModeConfiguration::enabled)
                .map(CombatModeConfiguration::mode)
                .filter(this::supports)
                .sorted()
                .collect(Collectors.toList());
    }

    public List<CombatMode> platformDisabledModes() {
        return configurations.values().stream()
                .filter(CombatModeConfiguration::enabled)
                .map(CombatModeConfiguration::mode)
                .filter(mode -> !supports(mode))
                .sorted()
                .collect(Collectors.toList());
    }
}
