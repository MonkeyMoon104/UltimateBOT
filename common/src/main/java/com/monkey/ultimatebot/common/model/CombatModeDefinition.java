package com.monkey.ultimatebot.common.model;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Public description of an available combat mode and its difficulty profiles. */
public record CombatModeDefinition(
        CombatMode mode,
        String displayName,
        boolean enabled,
        String iconMaterial,
        Set<CombatCapability> capabilities,
        Map<DifficultyTier, CombatTuning> profiles) {

    public CombatModeDefinition {
        Objects.requireNonNull(mode, "mode");
        displayName = requireText(displayName, "displayName");
        iconMaterial = requireText(iconMaterial, "iconMaterial");
        capabilities = Set.copyOf(Objects.requireNonNull(capabilities, "capabilities"));
        EnumMap<DifficultyTier, CombatTuning> profileCopy = new EnumMap<>(Objects.requireNonNull(profiles, "profiles"));
        for (DifficultyTier difficulty : DifficultyTier.values()) {
            Objects.requireNonNull(profileCopy.get(difficulty), "profiles[" + difficulty + "]");
        }
        profiles = Map.copyOf(profileCopy);
    }

    /** Returns the configured profile for a difficulty tier. */
    public CombatTuning profile(DifficultyTier difficulty) {
        CombatTuning tuning = profiles.get(Objects.requireNonNull(difficulty, "difficulty"));
        if (tuning == null) {
            throw new IllegalStateException("Missing " + mode + " profile for " + difficulty);
        }
        return tuning;
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return value;
    }
}
