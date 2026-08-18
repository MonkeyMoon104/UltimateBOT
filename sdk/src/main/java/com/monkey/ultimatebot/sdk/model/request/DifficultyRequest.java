package com.monkey.ultimatebot.sdk.model.request;

import com.monkey.ultimatebot.common.model.DifficultyTier;
import java.util.Objects;

/** Runtime request for a bot difficulty. */
public final class DifficultyRequest {
    private final DifficultyTier difficulty;

    public DifficultyRequest(DifficultyTier difficulty) {

        Objects.requireNonNull(difficulty, "difficulty");
        this.difficulty = difficulty;
    }

    public DifficultyTier difficulty() {
        return difficulty;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DifficultyRequest)) {
            return false;
        }
        DifficultyRequest other = (DifficultyRequest) obj;
        return java.util.Objects.equals(difficulty, other.difficulty);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(difficulty);
    }

    @Override
    public String toString() {
        return "DifficultyRequest[difficulty=" + difficulty + "]";
    }
}
