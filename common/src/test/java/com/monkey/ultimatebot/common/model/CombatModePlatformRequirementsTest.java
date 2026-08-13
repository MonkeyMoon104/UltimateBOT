package com.monkey.ultimatebot.common.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumSet;
import org.junit.jupiter.api.Test;

class CombatModePlatformRequirementsTest {
    @Test
    void swordAndWaterRequireNoPlatformCapabilities() {
        assertThat(CombatMode.SWORD.requiredPlatformCapabilities()).isEmpty();
        assertThat(CombatMode.WATER.requiredPlatformCapabilities()).isEmpty();
        assertThat(CombatMode.SWORD.supportedBy(EnumSet.noneOf(PlatformCapability.class))).isTrue();
    }

    @Test
    void maceRequiresMaceAndWindCharge() {
        assertThat(CombatMode.MACE.requiredPlatformCapabilities())
                .containsExactlyInAnyOrder(PlatformCapability.MACE, PlatformCapability.WIND_CHARGE);
        assertThat(CombatMode.MACE.supportedBy(EnumSet.of(PlatformCapability.MACE))).isFalse();
        assertThat(CombatMode.MACE.supportedBy(
                        EnumSet.of(PlatformCapability.MACE, PlatformCapability.WIND_CHARGE)))
                .isTrue();
    }

    @Test
    void cartRequiresNetheriteAndTntMinecart() {
        assertThat(CombatMode.CART.requiredPlatformCapabilities())
                .containsExactlyInAnyOrder(PlatformCapability.NETHERITE, PlatformCapability.TNT_MINECART);
    }

    @Test
    void crystalRequiresCrystalAnchorAndNetherite() {
        assertThat(CombatMode.CRYSTAL.requiredPlatformCapabilities())
                .containsExactlyInAnyOrder(
                        PlatformCapability.END_CRYSTAL,
                        PlatformCapability.RESPAWN_ANCHOR,
                        PlatformCapability.NETHERITE);
    }
}
