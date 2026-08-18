package com.monkey.ultimatebot.combat.mode.shared;

import static org.assertj.core.api.Assertions.assertThat;

import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;

class CobwebCombatAwarenessTest {
    @Test
    void detectsMovementNearTheWebBoundary() {
        assertThat(CobwebCombatAwareness.nearlyExiting(new Vector(10.9D, 64.0D, 20.5D), new Vector(0.03D, 0.0D, 0.0D)))
                .isTrue();
    }

    @Test
    void keepsStationaryAndCenteredTargetsContained() {
        assertThat(CobwebCombatAwareness.nearlyExiting(new Vector(10.5D, 64.0D, 20.5D), new Vector(0.03D, 0.0D, 0.0D)))
                .isFalse();
        assertThat(CobwebCombatAwareness.nearlyExiting(new Vector(10.95D, 64.0D, 20.5D), new Vector()))
                .isFalse();
    }
}
