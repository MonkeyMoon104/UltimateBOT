package com.monkey.ultimatebot.combat.mode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class UnderwaterMotionPlannerTest {
    @Test
    void pursuitUsesTheFullThreeDimensionalDirection() {
        Vec3 velocity = UnderwaterMotionPlanner.pursue(Vec3.ZERO, new Vec3(3.0D, 4.0D, 0.0D), Vec3.ZERO, 0.4D);

        assertThat(velocity.x).isPositive();
        assertThat(velocity.y).isPositive();
        assertThat(velocity.z).isZero();
        assertThat(velocity.length()).isLessThanOrEqualTo(0.4D);
    }

    @Test
    void orbitStrafesWhileCorrectingTowardTheTargetDepth() {
        Vec3 velocity =
                UnderwaterMotionPlanner.orbit(Vec3.ZERO, new Vec3(4.0D, 2.0D, 0.0D), Vec3.ZERO, 0.0D, 0.18D, 1.0D);

        assertThat(velocity.x).isZero();
        assertThat(velocity.y).isPositive();
        assertThat(velocity.z).isPositive();
    }

    @Test
    void recoveryCanMoveAwayWithoutForcingTheBotTowardTheSurface() {
        Vec3 velocity = UnderwaterMotionPlanner.orbit(
                new Vec3(0.0D, 8.0D, 0.0D), new Vec3(4.0D, 6.0D, 0.0D), Vec3.ZERO, -0.16D, 0.12D, -1.0D);

        assertThat(velocity.x).isNegative();
        assertThat(velocity.y).isNegative();
    }

    @Test
    void pursuitRejectsInvalidSpeed() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> UnderwaterMotionPlanner.pursue(Vec3.ZERO, Vec3.ZERO, Vec3.ZERO, 0.0D));
    }
}
