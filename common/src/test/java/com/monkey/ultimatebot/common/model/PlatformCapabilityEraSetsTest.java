package com.monkey.ultimatebot.common.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PlatformCapabilityEraSetsTest {

    @Test
    void noneIsEmpty() {
        assertThat(PlatformCapability.none()).isEmpty();
    }

    @Test
    void through1_8AllowsSwordWaterButNotNetheriteOrMace() {
        assertThat(CombatMode.SWORD.supportedBy(PlatformCapability.through1_8())).isTrue();
        assertThat(CombatMode.WATER.supportedBy(PlatformCapability.through1_8())).isTrue();
        assertThat(CombatMode.UHC.supportedBy(PlatformCapability.through1_8())).isTrue();
        assertThat(CombatMode.NETHERITE_POT.supportedBy(PlatformCapability.through1_8())).isFalse();
        assertThat(CombatMode.MACE.supportedBy(PlatformCapability.through1_8())).isFalse();
        assertThat(PlatformCapability.through1_8())
                .contains(PlatformCapability.TNT_MINECART)
                .doesNotContain(
                        PlatformCapability.OFFHAND,
                        PlatformCapability.SHIELD,
                        PlatformCapability.NETHERITE,
                        PlatformCapability.MACE);
    }

    @Test
    void through1_9AllowsUhcCrystalButNotTridentOrNetheriteModes() {
        assertThat(CombatMode.UHC.supportedBy(PlatformCapability.through1_9())).isTrue();
        assertThat(CombatMode.TRIDENT.supportedBy(PlatformCapability.through1_9())).isFalse();
        assertThat(CombatMode.CART.supportedBy(PlatformCapability.through1_9())).isTrue();
        assertThat(CombatMode.CRYSTAL.supportedBy(PlatformCapability.through1_9())).isTrue();
        assertThat(PlatformCapability.through1_9())
                .contains(
                        PlatformCapability.OFFHAND,
                        PlatformCapability.SHIELD,
                        PlatformCapability.COMBAT_COOLDOWN,
                        PlatformCapability.SWEEP_ATTACK,
                        PlatformCapability.END_CRYSTAL)
                .doesNotContain(PlatformCapability.TOTEM, PlatformCapability.TRIDENT, PlatformCapability.NETHERITE);
    }

    @Test
    void through1_11AddsTotem() {
        assertThat(PlatformCapability.through1_11())
                .containsAll(PlatformCapability.through1_9())
                .contains(PlatformCapability.TOTEM, PlatformCapability.END_CRYSTAL)
                .doesNotContain(PlatformCapability.TRIDENT, PlatformCapability.NETHERITE);
    }

    @Test
    void through1_13AddsTrident() {
        assertThat(CombatMode.TRIDENT.supportedBy(PlatformCapability.through1_13())).isTrue();
        assertThat(CombatMode.AXE_SHIELD.supportedBy(PlatformCapability.through1_13())).isTrue();
        assertThat(CombatMode.SMP.supportedBy(PlatformCapability.through1_13())).isTrue();
        assertThat(CombatMode.CRYSTAL.supportedBy(PlatformCapability.through1_13())).isTrue();
        assertThat(CombatMode.CART.supportedBy(PlatformCapability.through1_13())).isTrue();
        assertThat(CombatMode.NETHERITE_POT.supportedBy(PlatformCapability.through1_13())).isFalse();
        assertThat(PlatformCapability.through1_13())
                .containsAll(PlatformCapability.through1_11())
                .contains(PlatformCapability.TRIDENT, PlatformCapability.END_CRYSTAL)
                .doesNotContain(PlatformCapability.NETHERITE, PlatformCapability.RESPAWN_ANCHOR);
    }

    @Test
    void through1_16UnlocksNetheriteEraModesButNotMace() {
        assertThat(CombatMode.NETHERITE_POT.supportedBy(PlatformCapability.through1_16())).isTrue();
        assertThat(CombatMode.SMP.supportedBy(PlatformCapability.through1_16())).isTrue();
        assertThat(CombatMode.CART.supportedBy(PlatformCapability.through1_16())).isTrue();
        assertThat(CombatMode.CRYSTAL.supportedBy(PlatformCapability.through1_16())).isTrue();
        assertThat(CombatMode.AXE_SHIELD.supportedBy(PlatformCapability.through1_16())).isTrue();
        assertThat(CombatMode.MACE.supportedBy(PlatformCapability.through1_16())).isFalse();
        assertThat(PlatformCapability.through1_16())
                .contains(
                        PlatformCapability.NETHERITE,
                        PlatformCapability.END_CRYSTAL,
                        PlatformCapability.RESPAWN_ANCHOR)
                .doesNotContain(
                        PlatformCapability.MACE, PlatformCapability.WIND_CHARGE, PlatformCapability.ARMOR_TRIM);
    }

    @Test
    void through1_19MatchesThrough1_16() {
        assertThat(PlatformCapability.through1_19()).isEqualTo(PlatformCapability.through1_16());
        assertThat(PlatformCapability.through1_19())
                .doesNotContain(
                        PlatformCapability.MACE, PlatformCapability.WIND_CHARGE, PlatformCapability.ARMOR_TRIM);
        assertThat(PlatformCapability.through1_19()).contains(PlatformCapability.NETHERITE, PlatformCapability.TOTEM);
    }

    @Test
    void through1_20IncludesArmorTrimButNotMace() {
        assertThat(PlatformCapability.through1_20())
                .contains(PlatformCapability.ARMOR_TRIM, PlatformCapability.NETHERITE)
                .doesNotContain(PlatformCapability.MACE, PlatformCapability.WIND_CHARGE);
    }

    @Test
    void allSupportedContainsArmorTrimAndMace() {
        assertThat(PlatformCapability.allSupported())
                .contains(
                        PlatformCapability.ARMOR_TRIM, PlatformCapability.MACE, PlatformCapability.WIND_CHARGE);
        assertThat(CombatMode.MACE.supportedBy(PlatformCapability.allSupported())).isTrue();
    }

    @Test
    void forMinecraftVersionMapsEras() {
        assertThat(PlatformCapability.forMinecraftVersion("1.7.10")).isEqualTo(PlatformCapability.through1_8());
        assertThat(PlatformCapability.forMinecraftVersion("1.8.8")).isEqualTo(PlatformCapability.through1_8());
        assertThat(PlatformCapability.forMinecraftVersion("1.9.4")).isEqualTo(PlatformCapability.through1_9());
        assertThat(PlatformCapability.forMinecraftVersion("1.11.2")).isEqualTo(PlatformCapability.through1_11());
        assertThat(PlatformCapability.forMinecraftVersion("1.13.2")).isEqualTo(PlatformCapability.through1_13());
        assertThat(PlatformCapability.forMinecraftVersion("1.16.5")).isEqualTo(PlatformCapability.through1_16());
        assertThat(PlatformCapability.forMinecraftVersion("1.19.4")).isEqualTo(PlatformCapability.through1_19());
        assertThat(PlatformCapability.forMinecraftVersion("1.20.4")).isEqualTo(PlatformCapability.through1_20());
        assertThat(PlatformCapability.forMinecraftVersion("1.21.1")).isEqualTo(PlatformCapability.allSupported());
        assertThat(PlatformCapability.forMinecraftVersion("26.1")).isEqualTo(PlatformCapability.allSupported());
        assertThat(PlatformCapability.forMinecraftVersion("unknown")).isEqualTo(PlatformCapability.none());
    }
}
