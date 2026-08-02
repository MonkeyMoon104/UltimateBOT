package com.monkey.ultimatebot.bot.ai.behavior;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FollowDistancePolicyTest {
    @Test
    void keepsStableHysteresisAroundFollowDistance() {
        FollowDistancePolicy policy = new FollowDistancePolicy();

        assertThat(policy.shouldAdvance(3.0D)).isFalse();
        assertThat(policy.shouldAdvance(3.9D)).isTrue();
        assertThat(policy.shouldAdvance(3.2D)).isTrue();
        assertThat(policy.shouldAdvance(2.8D)).isFalse();
        assertThat(policy.shouldAdvance(3.4D)).isFalse();
    }
}
