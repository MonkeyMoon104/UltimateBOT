package com.monkey.ultimatebot.common.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumSet;
import org.junit.jupiter.api.Test;

class CombatModePlatformRequirementsTest {
    @Test
    void swordWaterAndUhcRequireNoPlatformCapabilities() {
        assertThat(CombatMode.SWORD.requiredPlatformCapabilities()).isEmpty();
        assertThat(CombatMode.WATER.requiredPlatformCapabilities()).isEmpty();
        assertThat(CombatMode.UHC.requiredPlatformCapabilities()).isEmpty();
        assertThat(CombatMode.SWORD.supportedBy(EnumSet.noneOf(PlatformCapability.class)))
                .isTrue();
        assertThat(CombatMode.UHC.supportedBy(EnumSet.noneOf(PlatformCapability.class)))
                .isTrue();
    }

    @Test
    void maceRequiresMaceAndWindCharge() {
        assertThat(CombatMode.MACE.requiredPlatformCapabilities())
                .containsExactlyInAnyOrder(PlatformCapability.MACE, PlatformCapability.WIND_CHARGE);
        assertThat(CombatMode.MACE.supportedBy(EnumSet.of(PlatformCapability.MACE)))
                .isFalse();
        assertThat(CombatMode.MACE.supportedBy(EnumSet.of(PlatformCapability.MACE, PlatformCapability.WIND_CHARGE)))
                .isTrue();
    }

    @Test
    void cartRequiresTntMinecartOnly() {
        assertThat(CombatMode.CART.requiredPlatformCapabilities()).containsExactly(PlatformCapability.TNT_MINECART);
    }

    @Test
    void crystalRequiresEndCrystalOnly() {
        assertThat(CombatMode.CRYSTAL.requiredPlatformCapabilities()).containsExactly(PlatformCapability.END_CRYSTAL);
    }

    @Test
    void axeShieldAndSmpDoNotRequireNetherite() {
        assertThat(CombatMode.AXE_SHIELD.requiredPlatformCapabilities())
                .containsExactlyInAnyOrder(
                        PlatformCapability.SHIELD, PlatformCapability.OFFHAND, PlatformCapability.COMBAT_COOLDOWN);
        assertThat(CombatMode.SMP.requiredPlatformCapabilities())
                .containsExactlyInAnyOrder(
                        PlatformCapability.SHIELD, PlatformCapability.TOTEM, PlatformCapability.OFFHAND);
        assertThat(CombatMode.NETHERITE_POT.requiredPlatformCapabilities()).contains(PlatformCapability.NETHERITE);
    }
}
