package com.monkey.ultimatebot.bot.ai.controllers.cpvp;

import static org.assertj.core.api.Assertions.assertThat;

import com.monkey.ultimatebot.bot.ai.difficulty.DifficultyLevel;
import org.junit.jupiter.api.Test;

class CrystalCombatPolicyTest {

    private final CrystalCombatPolicy policy = new CrystalCombatPolicy();

    @Test
    void scalesActionBudgetWithDifficulty() {
        policy.setDifficulty(DifficultyLevel.EASY);
        assertThat(policy.attackBurstAttempts()).isEqualTo(1);
        assertThat(policy.actionCyclesPerTick()).isEqualTo(1);

        policy.setDifficulty(DifficultyLevel.GOD);
        assertThat(policy.attackBurstAttempts()).isEqualTo(4);
        assertThat(policy.placementBurstAttempts()).isEqualTo(3);
        assertThat(policy.actionCyclesPerTick()).isEqualTo(2);
    }

    @Test
    void reducesPositionReuseDelayForStrongerProfiles() {
        policy.setDifficulty(DifficultyLevel.EASY);
        long easyDelay = policy.positionReuseDelayMs();

        policy.setDifficulty(DifficultyLevel.GOD);
        assertThat(policy.positionReuseDelayMs()).isLessThan(easyDelay);
        assertThat(policy.desiredStrongPositionCount()).isGreaterThan(1);
    }
}
