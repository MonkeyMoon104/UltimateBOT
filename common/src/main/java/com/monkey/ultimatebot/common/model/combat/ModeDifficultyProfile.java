package com.monkey.ultimatebot.common.model.combat;

import java.util.Objects;

public final class ModeDifficultyProfile {
    private final CombatMode mode;
    private final DifficultyTier difficulty;
    private final CombatTuning tuning;

    public ModeDifficultyProfile(CombatMode mode, DifficultyTier difficulty, CombatTuning tuning) {
        this.mode = Objects.requireNonNull(mode, "mode");
        this.difficulty = Objects.requireNonNull(difficulty, "difficulty");
        this.tuning = Objects.requireNonNull(tuning, "tuning");
    }

    public static ModeDifficultyProfile of(CombatMode mode, DifficultyTier difficulty, CombatTuning tuning) {
        return new ModeDifficultyProfile(mode, difficulty, tuning);
    }

    public CombatMode mode() {
        return mode;
    }

    public DifficultyTier difficulty() {
        return difficulty;
    }

    public CombatTuning tuning() {
        return tuning;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ModeDifficultyProfile)) {
            return false;
        }
        ModeDifficultyProfile other = (ModeDifficultyProfile) obj;
        return mode.equals(other.mode) && difficulty == other.difficulty && tuning.equals(other.tuning);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mode, difficulty, tuning);
    }

    @Override
    public String toString() {
        return "ModeDifficultyProfile[mode=" + mode + ", difficulty=" + difficulty + ", tuning=" + tuning + ']';
    }
}
