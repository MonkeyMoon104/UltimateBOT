package com.monkey.ultimatebot.combat.mode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;

class CartExplosiveSequenceTest {
    @Test
    void detonatesOnlyWhenTheIgnitionArrowReachesTheCart() {
        assertThat(CartExplosiveSequence.isArrowImpact(1.8D)).isTrue();
        assertThat(CartExplosiveSequence.isArrowImpact(1.81D)).isFalse();
        assertThat(CartExplosiveSequence.isArrowImpact(Double.NaN)).isFalse();
    }

    @Test
    void railCandidatesFallBackFromPredictionToCurrentTargetBlock() {
        World world = mock(World.class);
        when(world.getUID()).thenReturn(UUID.randomUUID());

        List<Location> candidates = CartExplosiveSequence.railCandidates(
                new Location(world, 4.2D, 64.0D, 7.8D), new Vector(0.8D, 0.2D, 0.0D));

        assertThat(candidates).hasSizeGreaterThanOrEqualTo(2);
        assertThat(candidates.getLast().getBlockX()).isEqualTo(4);
        assertThat(candidates.getLast().getBlockZ()).isEqualTo(7);
    }
}
