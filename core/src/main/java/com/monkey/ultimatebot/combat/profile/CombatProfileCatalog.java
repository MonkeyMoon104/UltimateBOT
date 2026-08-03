package com.monkey.ultimatebot.combat.profile;

import com.monkey.ultimatebot.common.model.CombatMode;
import com.monkey.ultimatebot.common.model.CombatTuning;
import com.monkey.ultimatebot.common.model.DifficultyTier;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class CombatProfileCatalog {
    private final Map<CombatMode, CombatModeConfiguration> configurations;

    public CombatProfileCatalog(Map<CombatMode, CombatModeConfiguration> configurations) {
        Objects.requireNonNull(configurations, "configurations");
        EnumMap<CombatMode, CombatModeConfiguration> copy = new EnumMap<>(CombatMode.class);
        copy.putAll(configurations);
        for (CombatMode mode : CombatMode.values()) {
            if (!copy.containsKey(mode)) {
                throw new IllegalArgumentException("Missing combat mode configuration for " + mode);
            }
        }
        this.configurations = Map.copyOf(copy);
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

    public List<CombatMode> enabledModes() {
        return configurations.values().stream()
                .filter(CombatModeConfiguration::enabled)
                .map(CombatModeConfiguration::mode)
                .sorted()
                .toList();
    }
}
