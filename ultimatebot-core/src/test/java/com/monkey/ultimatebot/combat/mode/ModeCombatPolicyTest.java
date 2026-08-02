package com.monkey.ultimatebot.combat.mode;

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
        assertThat(ModeCombatPolicy.isCartOpportunity(8.0D, 64.0D, 64.0D, 0.7D, false))
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
}
