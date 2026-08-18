package com.monkey.ultimatebot.combat.mode.water;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;

class UnderwaterMotionPlannerTest {
    @Test
    void pursuitUsesTheFullThreeDimensionalDirection() {
        Vector velocity =
                UnderwaterMotionPlanner.pursue(new Vector(), new Vector(3.0D, 4.0D, 0.0D), new Vector(), 0.4D);

        assertThat(velocity.getX()).isPositive();
        assertThat(velocity.getY()).isPositive();
        assertThat(velocity.getZ()).isZero();
        assertThat(velocity.length()).isLessThanOrEqualTo(0.4D);
    }

    @Test
    void orbitStrafesWhileCorrectingTowardTheTargetDepth() {
        Vector velocity = UnderwaterMotionPlanner.orbit(
                new Vector(), new Vector(4.0D, 2.0D, 0.0D), new Vector(), 0.0D, 0.18D, 1.0D);

        assertThat(velocity.getX()).isZero();
        assertThat(velocity.getY()).isPositive();
        assertThat(velocity.getZ()).isPositive();
    }

    @Test
    void recoveryCanMoveAwayWithoutForcingTheBotTowardTheSurface() {
        Vector velocity = UnderwaterMotionPlanner.orbit(
                new Vector(0.0D, 8.0D, 0.0D), new Vector(4.0D, 6.0D, 0.0D), new Vector(), -0.16D, 0.12D, -1.0D);

        assertThat(velocity.getX()).isNegative();
        assertThat(velocity.getY()).isNegative();
    }

    @Test
    void pursuitRejectsInvalidSpeed() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> UnderwaterMotionPlanner.pursue(new Vector(), new Vector(), new Vector(), 0.0D));
    }
}
