package com.monkey.ultimatebot.combat.mode;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CartExplosiveSequenceTest {
    @Test
    void detonatesOnlyWhenTheIgnitionArrowReachesTheCart() {
        assertThat(CartExplosiveSequence.isArrowImpact(1.8D)).isTrue();
        assertThat(CartExplosiveSequence.isArrowImpact(1.81D)).isFalse();
        assertThat(CartExplosiveSequence.isArrowImpact(Double.NaN)).isFalse();
    }
}
