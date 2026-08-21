package com.monkey.ultimatebot.combat.profile;

import com.monkey.ultimatebot.common.model.combat.CombatMode;
import com.monkey.ultimatebot.common.model.combat.CombatTuning;
import com.monkey.ultimatebot.common.model.combat.DifficultyTier;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public final class CombatModeConfiguration {
    private final CombatMode mode;
    private final boolean enabled;
    private final String iconMaterial;
    private final Map<DifficultyTier, CombatTuning> profiles;

    public CombatModeConfiguration(
            CombatMode mode, boolean enabled, String iconMaterial, Map<DifficultyTier, CombatTuning> profiles) {

        Objects.requireNonNull(mode, "mode");
        Objects.requireNonNull(iconMaterial, "iconMaterial");
        Objects.requireNonNull(profiles, "profiles");
        profiles = com.monkey.ultimatebot.common.util.ImmutableCollections.copyOf(new EnumMap<>(profiles));
        for (DifficultyTier difficulty : DifficultyTier.values()) {
            if (!profiles.containsKey(difficulty)) {
                throw new IllegalArgumentException("Missing " + mode + " profile for " + difficulty);
            }
        }
        this.mode = mode;
        this.enabled = enabled;
        this.iconMaterial = iconMaterial;
        this.profiles = profiles;
    }

    public CombatMode mode() {
        return mode;
    }

    public boolean enabled() {
        return enabled;
    }

    public String iconMaterial() {
        return iconMaterial;
    }

    public Map<DifficultyTier, CombatTuning> profiles() {
        return profiles;
    }

    public CombatTuning profile(DifficultyTier difficulty) {
        CombatTuning tuning = profiles.get(Objects.requireNonNull(difficulty, "difficulty"));
        if (tuning == null) {
            throw new IllegalStateException("Missing " + mode + " profile for " + difficulty);
        }
        return tuning;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CombatModeConfiguration)) {
            return false;
        }
        CombatModeConfiguration other = (CombatModeConfiguration) obj;
        return java.util.Objects.equals(mode, other.mode)
                && enabled == other.enabled
                && java.util.Objects.equals(iconMaterial, other.iconMaterial)
                && java.util.Objects.equals(profiles, other.profiles);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(mode, enabled, iconMaterial, profiles);
    }

    @Override
    public String toString() {
        return "CombatModeConfiguration[mode=" + mode + ", enabled=" + enabled + ", iconMaterial=" + iconMaterial
                + ", profiles=" + profiles + "]";
    }
}
