package com.monkey.ultimatebot.common.model;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/** Public description of an available combat mode and its difficulty profiles. */
public record CombatModeDefinition(
        CombatMode mode,
        String displayName,
        boolean enabled,
        String iconMaterial,
        Set<CombatCapability> capabilities,
        Map<DifficultyTier, CombatTuning> profiles,
        String provider,
        List<String> description,
        String permission,
        int order,
        @Nullable BrainKey brain) {

    public CombatModeDefinition {
        Objects.requireNonNull(mode, "mode");
        displayName = requireText(displayName, "displayName");
        iconMaterial = requireText(iconMaterial, "iconMaterial");
        provider = requireText(provider, "provider");
        description = List.copyOf(Objects.requireNonNull(description, "description"));
        permission = Objects.requireNonNull(permission, "permission").trim();
        capabilities = Set.copyOf(Objects.requireNonNull(capabilities, "capabilities"));
        EnumMap<DifficultyTier, CombatTuning> profileCopy = new EnumMap<>(Objects.requireNonNull(profiles, "profiles"));
        for (DifficultyTier difficulty : DifficultyTier.values()) {
            Objects.requireNonNull(profileCopy.get(difficulty), "profiles[" + difficulty + "]");
        }
        profiles = Map.copyOf(profileCopy);
    }

    public CombatModeDefinition(
            CombatMode mode,
            String displayName,
            boolean enabled,
            String iconMaterial,
            Set<CombatCapability> capabilities,
            Map<DifficultyTier, CombatTuning> profiles) {
        this(
                mode,
                displayName,
                enabled,
                iconMaterial,
                capabilities,
                profiles,
                mode.namespace(),
                List.of(),
                "",
                0,
                null);
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
