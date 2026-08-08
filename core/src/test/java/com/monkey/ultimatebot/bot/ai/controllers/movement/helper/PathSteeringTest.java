package com.monkey.ultimatebot.bot.ai.controllers.movement.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;

class PathSteeringTest {
    @Test
    void turnsProgressivelyInsteadOfSnappingToNewDirection() {
        PathSteering steering = new PathSteering();
        steering.update(new Vector(), new Vector(1.0D, 0.0D, 0.0D));

        Vector firstTurn = steering.update(new Vector(), new Vector(0.0D, 0.0D, 1.0D));
        Vector secondTurn = steering.update(new Vector(), new Vector(0.0D, 0.0D, 1.0D));

        assertThat(firstTurn.getX()).isPositive();
        assertThat(firstTurn.getZ()).isPositive();
        assertThat(secondTurn.getZ()).isGreaterThan(firstTurn.getZ());
        assertThat(secondTurn.getX()).isLessThan(firstTurn.getX());
    }

    @Test
    void resetStartsFromCurrentVelocity() {
        PathSteering steering = new PathSteering();
        steering.update(new Vector(), new Vector(1.0D, 0.0D, 0.0D));
        steering.reset();

        Vector direction = steering.update(new Vector(0.0D, 0.0D, 0.25D), new Vector(0.0D, 0.0D, 1.0D));

        assertThat(direction.getX()).isCloseTo(0.0D, within(1.0E-9D));
        assertThat(direction.getZ()).isCloseTo(1.0D, within(1.0E-9D));
    }
}
