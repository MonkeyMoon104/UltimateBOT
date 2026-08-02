package com.monkey.ultimatebot.common.model;

import java.util.Objects;

/** Fully resolved tuning for one combat mode and difficulty pair. */
public record ModeDifficultyProfile(CombatMode mode, DifficultyTier difficulty, CombatTuning tuning) {
    public ModeDifficultyProfile {
        Objects.requireNonNull(mode, "mode");
        Objects.requireNonNull(difficulty, "difficulty");
        Objects.requireNonNull(tuning, "tuning");
    }

    public static ModeDifficultyProfile of(CombatMode mode, DifficultyTier difficulty, CombatTuning tuning) {
        return new ModeDifficultyProfile(mode, difficulty, tuning);
    }
}
