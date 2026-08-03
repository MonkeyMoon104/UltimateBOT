package com.monkey.ultimatebot.combat.mode.shared;

import static org.assertj.core.api.Assertions.assertThat;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class CobwebCombatAwarenessTest {
    @Test
    void detectsMovementNearTheWebBoundary() {
        assertThat(CobwebCombatAwareness.nearlyExiting(new Vec3(10.9D, 64.0D, 20.5D), new Vec3(0.03D, 0.0D, 0.0D)))
                .isTrue();
    }

    @Test
    void keepsStationaryAndCenteredTargetsContained() {
        assertThat(CobwebCombatAwareness.nearlyExiting(new Vec3(10.5D, 64.0D, 20.5D), new Vec3(0.03D, 0.0D, 0.0D)))
                .isFalse();
        assertThat(CobwebCombatAwareness.nearlyExiting(new Vec3(10.95D, 64.0D, 20.5D), Vec3.ZERO))
                .isFalse();
    }
}
