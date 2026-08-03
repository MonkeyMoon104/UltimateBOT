package com.monkey.ultimatebot.combat.mode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.SplittableRandom;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;

class WebTrapPlannerTest {
    @Test
    void stationaryTargetIsTrappedAtFeetHeadAndApproachSide() {
        World world = world();

        List<Location> plan = WebTrapPlanner.plan(
                new Location(world, 0.2D, 64.0D, 0.2D),
                new Vector(),
                true,
                new Vector(0.0D, 0.0D, 1.0D),
                new SplittableRandom(4L));

        assertThat(plan).extracting(Location::getBlockY).contains(64, 65);
        assertThat(plan).anyMatch(location -> location.getBlockX() == 0 && location.getBlockZ() == 1);
    }

    @Test
    void jumpingTargetAddsAWebAtItsPredictedHeadPosition() {
        World world = world();

        List<Location> plan = WebTrapPlanner.plan(
                new Location(world, 0.2D, 64.0D, 0.2D),
                new Vector(0.4D, 0.4D, 0.0D),
                false,
                new Vector(1.0D, 0.0D, 0.0D),
                new SplittableRandom(8L));

        assertThat(plan).anyMatch(location -> location.getBlockX() == 1 && location.getBlockY() == 65);
    }

    private static World world() {
        World world = mock(World.class);
        when(world.getUID()).thenReturn(UUID.randomUUID());
        return world;
    }
}
