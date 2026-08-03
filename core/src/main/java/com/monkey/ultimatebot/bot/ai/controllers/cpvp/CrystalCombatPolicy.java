package com.monkey.ultimatebot.bot.ai.controllers.cpvp;

import com.monkey.ultimatebot.bot.ai.difficulty.DifficultyLevel;
import java.util.Objects;

final class CrystalCombatPolicy {

    private DifficultyLevel difficulty = DifficultyLevel.NORMAL;

    void setDifficulty(DifficultyLevel difficulty) {
        this.difficulty = Objects.requireNonNull(difficulty, "difficulty");
    }

    int attackBurstAttempts() {
        return switch (difficulty) {
            case GOD -> 4;
            case HARD -> 3;
            case MEDIUM, NORMAL -> 2;
            default -> 1;
        };
    }

    int placementBurstAttempts() {
        return switch (difficulty) {
            case GOD, HARD -> 3;
            case MEDIUM, NORMAL -> 2;
            default -> 1;
        };
    }

    int actionCyclesPerTick() {
        return switch (difficulty) {
            case GOD, HARD, MEDIUM -> 2;
            default -> 1;
        };
    }

    long positionReuseDelayMs() {
        return switch (difficulty) {
            case GOD -> 300L;
            case HARD -> 380L;
            case MEDIUM -> 550L;
            case NORMAL -> 700L;
            default -> 900L;
        };
    }

    double strongScoreOffset() {
        return switch (difficulty) {
            case GOD -> 0.9D;
            case HARD -> 1.1D;
            case MEDIUM -> 1.25D;
            case NORMAL -> 1.4D;
            default -> 1.65D;
        };
    }

    int desiredStrongPositionCount() {
        return switch (difficulty) {
            case GOD -> 3;
            case HARD, MEDIUM -> 2;
            default -> 1;
        };
    }

    double maxUsefulTargetDistance() {
        return switch (difficulty) {
            case GOD -> 5.2D;
            case HARD -> 5.8D;
            case MEDIUM -> 6.3D;
            case NORMAL -> 6.6D;
            default -> 6.9D;
        };
    }
}
