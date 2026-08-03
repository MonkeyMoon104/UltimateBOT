package com.monkey.ultimatebot.combat.profile;

import com.monkey.ultimatebot.common.model.CombatMode;
import com.monkey.ultimatebot.common.model.CombatTuning;
import com.monkey.ultimatebot.common.model.DifficultyTier;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public record CombatModeConfiguration(
        CombatMode mode, boolean enabled, String iconMaterial, Map<DifficultyTier, CombatTuning> profiles) {
    public CombatModeConfiguration {
        Objects.requireNonNull(mode, "mode");
        Objects.requireNonNull(iconMaterial, "iconMaterial");
        Objects.requireNonNull(profiles, "profiles");
        profiles = Map.copyOf(new EnumMap<>(profiles));
        for (DifficultyTier difficulty : DifficultyTier.values()) {
            if (!profiles.containsKey(difficulty)) {
                throw new IllegalArgumentException("Missing " + mode + " profile for " + difficulty);
            }
        }
    }

    public CombatTuning profile(DifficultyTier difficulty) {
        CombatTuning tuning = profiles.get(Objects.requireNonNull(difficulty, "difficulty"));
        if (tuning == null) {
            throw new IllegalStateException("Missing " + mode + " profile for " + difficulty);
        }
        return tuning;
    }
}
