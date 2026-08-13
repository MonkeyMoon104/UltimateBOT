package com.monkey.ultimatebot.world;

import static org.assertj.core.api.Assertions.assertThat;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class WorldProtectionPolicyTest {
    @Test
    void antiDupeSuppressesDropsOnlyForTrackedBotBlocks() {
        assertThat(WorldProtectionPolicy.shouldSuppressDrops(true, true)).isTrue();
        assertThat(WorldProtectionPolicy.shouldSuppressDrops(true, false)).isFalse();
        assertThat(WorldProtectionPolicy.shouldSuppressDrops(false, true)).isFalse();
    }

    @Test
    void shutdownRestoresOnlyTrackedMatchingBotBlocksWhenAntiDupeIsEnabled() {
        assertThat(WorldProtectionPolicy.shouldRestoreOnShutdown(true, true, true))
                .isTrue();
        assertThat(WorldProtectionPolicy.shouldRestoreOnShutdown(true, false, true))
                .isFalse();
        assertThat(WorldProtectionPolicy.shouldRestoreOnShutdown(true, true, false))
                .isFalse();
        assertThat(WorldProtectionPolicy.shouldRestoreOnShutdown(false, true, true))
                .isFalse();
    }

    @Test
    void shutdownRemovesOnlyTrackedBotEntitiesWhenAntiDupeIsEnabled() {
        assertThat(WorldProtectionPolicy.shouldRemoveEntityOnShutdown(true, true))
                .isTrue();
        assertThat(WorldProtectionPolicy.shouldRemoveEntityOnShutdown(true, false))
                .isFalse();
        assertThat(WorldProtectionPolicy.shouldRemoveEntityOnShutdown(false, true))
                .isFalse();
    }

    @Test
    void cobwebsAndWaterRequireSolidGroundOrAnotherCobwebBelow() {
        assertThat(WorldProtectionPolicy.hasPlacementSupport(Material.COBWEB, false))
                .isFalse();
        assertThat(WorldProtectionPolicy.hasPlacementSupport(Material.COBWEB, true))
                .isTrue();
        assertThat(WorldProtectionPolicy.hasPlacementSupport(Material.WATER, false))
                .isFalse();
        assertThat(WorldProtectionPolicy.hasPlacementSupport(Material.WATER, true))
                .isTrue();
        assertThat(WorldProtectionPolicy.hasPlacementSupport(Material.SPONGE, false))
                .isTrue();
    }
}
