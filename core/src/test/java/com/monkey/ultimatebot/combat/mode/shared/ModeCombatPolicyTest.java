package com.monkey.ultimatebot.combat.mode.shared;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ModeCombatPolicyTest {
    @Test
    void maceLaunchRequiresGroundRangeAndCooldown() {
        assertThat(ModeCombatPolicy.canLaunchMace(4.5D, true, true)).isTrue();
        assertThat(ModeCombatPolicy.canLaunchMace(4.6D, true, true)).isFalse();
        assertThat(ModeCombatPolicy.canLaunchMace(3.0D, false, true)).isFalse();
        assertThat(ModeCombatPolicy.canLaunchMace(3.0D, true, false)).isFalse();
    }

    @Test
    void waterCombatRequiresBothEntitiesInWater() {
        assertThat(ModeCombatPolicy.canFightInWater(true, true)).isTrue();
        assertThat(ModeCombatPolicy.canFightInWater(true, false)).isFalse();
        assertThat(ModeCombatPolicy.canFightInWater(false, true)).isFalse();
    }

    @Test
    void cartWindowRejectsHighGroundAndBadDistance() {
        assertThat(ModeCombatPolicy.isCartOpportunity(5.0D, 64.0D, 64.0D, 0.7D, false))
                .isTrue();
        assertThat(ModeCombatPolicy.isCartOpportunity(5.0D, 66.0D, 64.0D, 0.7D, false))
                .isFalse();
        assertThat(ModeCombatPolicy.isCartOpportunity(10.1D, 64.0D, 64.0D, 0.7D, false))
                .isFalse();
        assertThat(ModeCombatPolicy.isCartOpportunity(0.9D, 64.0D, 64.0D, 0.7D, true))
                .isFalse();
        assertThat(ModeCombatPolicy.isCartOpportunity(5.0D, 64.0D, 64.0D, 1.0D, true))
                .isTrue();
    }

    @Test
    void potionWindowHonorsHealthAndCooldown() {
        assertThat(ModeCombatPolicy.shouldUsePotions(0.4D, 0.5D, true)).isTrue();
        assertThat(ModeCombatPolicy.shouldUsePotions(0.6D, 0.5D, true)).isFalse();
        assertThat(ModeCombatPolicy.shouldUsePotions(0.4D, 0.5D, false)).isFalse();
    }

    @Test
    void projectileSpreadIsSafeAtPerfectAndOutOfRangeAccuracy() {
        assertThat(ModeCombatPolicy.projectileSpread(1.0D)).isZero();
        assertThat(ModeCombatPolicy.projectileSpread(1.5D)).isZero();
        assertThat(ModeCombatPolicy.projectileSpread(0.5D)).isEqualTo(0.12D);
        assertThat(ModeCombatPolicy.projectileSpread(-0.5D)).isEqualTo(0.24D);
    }

    @Test
    void shieldThreatDetectsReadyAndRushingPlayers() {
        assertThat(ModeCombatPolicy.isIncomingPlayerAttack(3.2D, 0.9D, false, false, 0.0D, 0.8D))
                .isTrue();
        assertThat(ModeCombatPolicy.isIncomingPlayerAttack(3.2D, 0.9D, false, false, 0.02D, 0.8D))
                .isTrue();
        assertThat(ModeCombatPolicy.isIncomingPlayerAttack(4.5D, 0.6D, false, false, 0.12D, 0.9D))
                .isTrue();
        assertThat(ModeCombatPolicy.isIncomingPlayerAttack(7.0D, 0.2D, false, true, 0.0D, 0.8D))
                .isTrue();
        assertThat(ModeCombatPolicy.isIncomingPlayerAttack(3.0D, 1.0D, true, true, 0.2D, 1.0D))
                .isFalse();
        assertThat(ModeCombatPolicy.isIncomingPlayerAttack(4.5D, 0.6D, false, false, 0.0D, 0.1D))
                .isFalse();
    }

    @Test
    void axeOpeningRequiresAStableSafeWindow() {
        assertThat(ModeCombatPolicy.isSafeAxeOpening(3.0D, 3.2D, false, true, 3))
                .isTrue();
        assertThat(ModeCombatPolicy.isSafeAxeOpening(3.0D, 3.2D, true, true, 3)).isFalse();
        assertThat(ModeCombatPolicy.isSafeAxeOpening(3.0D, 3.2D, false, false, 3))
                .isFalse();
        assertThat(ModeCombatPolicy.isSafeAxeOpening(3.0D, 3.2D, false, true, 2))
                .isFalse();
    }

    @Test
    void shieldCounterUsesRangeReadinessAndDifficultyChance() {
        assertThat(ModeCombatPolicy.shouldCounterShieldImpact(3.0D, 3.2D, true, 0.6D, 0.5D))
                .isTrue();
        assertThat(ModeCombatPolicy.shouldCounterShieldImpact(3.3D, 3.2D, true, 1.0D, 0.0D))
                .isFalse();
        assertThat(ModeCombatPolicy.shouldCounterShieldImpact(3.0D, 3.2D, false, 1.0D, 0.0D))
                .isFalse();
        assertThat(ModeCombatPolicy.shouldCounterShieldImpact(3.0D, 3.2D, true, 0.0D, 0.36D))
                .isFalse();
    }

    @Test
    void bowRequiresFullTwentyTickDraw() {
        assertThat(ModeCombatPolicy.isBowFullyDrawn(19)).isFalse();
        assertThat(ModeCombatPolicy.isBowFullyDrawn(20)).isTrue();
        assertThat(ModeCombatPolicy.isBowFullyDrawn(25)).isTrue();
    }

    @Test
    void ignitionDrawUsesVanillaPowerCurveAndDistance() {
        assertThat(ModeCombatPolicy.bowPower(0)).isZero();
        assertThat(ModeCombatPolicy.bowPower(20)).isEqualTo(1.0D);
        assertThat(ModeCombatPolicy.bowPower(30)).isEqualTo(1.0D);
        assertThat(ModeCombatPolicy.ignitionBowDrawTicks(3.0D)).isLessThan(ModeCombatPolicy.ignitionBowDrawTicks(9.0D));
        assertThat(ModeCombatPolicy.ignitionBowDrawTicks(30.0D)).isEqualTo(20);
    }
}
