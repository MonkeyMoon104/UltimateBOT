package com.monkey.mcbot.bot.ai.controllers.movement.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class PathSteeringTest {
    @Test
    void turnsProgressivelyInsteadOfSnappingToNewDirection() {
        PathSteering steering = new PathSteering();
        steering.update(Vec3.ZERO, new Vec3(1.0D, 0.0D, 0.0D));

        Vec3 firstTurn = steering.update(Vec3.ZERO, new Vec3(0.0D, 0.0D, 1.0D));
        Vec3 secondTurn = steering.update(Vec3.ZERO, new Vec3(0.0D, 0.0D, 1.0D));

        assertThat(firstTurn.x).isPositive();
        assertThat(firstTurn.z).isPositive();
        assertThat(secondTurn.z).isGreaterThan(firstTurn.z);
        assertThat(secondTurn.x).isLessThan(firstTurn.x);
    }

    @Test
    void resetStartsFromCurrentVelocity() {
        PathSteering steering = new PathSteering();
        steering.update(Vec3.ZERO, new Vec3(1.0D, 0.0D, 0.0D));
        steering.reset();

        Vec3 direction = steering.update(new Vec3(0.0D, 0.0D, 0.25D), new Vec3(0.0D, 0.0D, 1.0D));

        assertThat(direction.x).isCloseTo(0.0D, within(1.0E-9D));
        assertThat(direction.z).isCloseTo(1.0D, within(1.0E-9D));
    }
}
