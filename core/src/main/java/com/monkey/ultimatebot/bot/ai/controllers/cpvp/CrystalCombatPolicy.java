package com.monkey.ultimatebot.bot.ai.controllers.cpvp;

import com.monkey.ultimatebot.bot.ai.difficulty.DifficultyLevel;
import java.util.Objects;

final class CrystalCombatPolicy {

    private DifficultyLevel difficulty = DifficultyLevel.NORMAL;

    void setDifficulty(DifficultyLevel difficulty) {
        this.difficulty = Objects.requireNonNull(difficulty, "difficulty");
    }

    int attackBurstAttempts() {
        switch (difficulty) {
            case GOD:
                return 4;
            case HARD:
                return 3;
            case MEDIUM:
            case NORMAL:
                return 2;
            default:
                return 1;
        }
    }

    int placementBurstAttempts() {
        switch (difficulty) {
            case GOD:
            case HARD:
                return 3;
            case MEDIUM:
            case NORMAL:
                return 2;
            default:
                return 1;
        }
    }

    int actionCyclesPerTick() {
        switch (difficulty) {
            case GOD:
            case HARD:
            case MEDIUM:
                return 2;
            default:
                return 1;
        }
    }

    long positionReuseDelayMs() {
        switch (difficulty) {
            case GOD:
                return 300L;
            case HARD:
                return 380L;
            case MEDIUM:
                return 550L;
            case NORMAL:
                return 700L;
            default:
                return 900L;
        }
    }

    double strongScoreOffset() {
        switch (difficulty) {
            case GOD:
                return 0.9D;
            case HARD:
                return 1.1D;
            case MEDIUM:
                return 1.25D;
            case NORMAL:
                return 1.4D;
            default:
                return 1.65D;
        }
    }

    int desiredStrongPositionCount() {
        switch (difficulty) {
            case GOD:
                return 3;
            case HARD:
            case MEDIUM:
                return 2;
            default:
                return 1;
        }
    }

    double maxUsefulTargetDistance() {
        switch (difficulty) {
            case GOD:
                return 5.2D;
            case HARD:
                return 5.8D;
            case MEDIUM:
                return 6.3D;
            case NORMAL:
                return 6.6D;
            default:
                return 6.9D;
        }
    }
}
