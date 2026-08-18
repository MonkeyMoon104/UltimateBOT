package com.monkey.ultimatebot.utils.armor;

import static org.assertj.core.api.Assertions.assertThat;

import com.monkey.ultimatebot.common.model.EquipmentSlotKind;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class ArmorCycleTest {

    @Test
    void cyclingWithinDiamondSkipsNetherite() {
        Material next = ArmorCycle.getNextArmor(
                Material.DIAMOND_HELMET, EquipmentSlotKind.HEAD, ArmorTier.LEATHER, ArmorTier.DIAMOND);
        assertThat(next).isEqualTo(Material.LEATHER_HELMET);
        assertThat(next).isNotEqualTo(Material.AIR);
    }

    @Test
    void cyclingFromDiamondWithNetheriteMaxUsesPlatformCap() {
        Material next = ArmorCycle.getNextArmor(
                Material.DIAMOND_HELMET, EquipmentSlotKind.HEAD, ArmorTier.LEATHER, ArmorTier.NETHERITE);
        if (Material.matchMaterial("NETHERITE_HELMET") == null) {
            assertThat(next).isEqualTo(Material.LEATHER_HELMET);
        } else {
            assertThat(next).isEqualTo(Material.NETHERITE_HELMET);
        }
        assertThat(next).isNotEqualTo(Material.AIR);
    }

    @Test
    void clampDropsTiersAbovePlatformMax() {
        Material clamped = ArmorCycle.clampArmor(
                Material.DIAMOND_HELMET, EquipmentSlotKind.HEAD, ArmorTier.LEATHER, ArmorTier.NETHERITE);
        assertThat(clamped).isNotEqualTo(Material.AIR);
        if (Material.matchMaterial("NETHERITE_HELMET") == null) {
            assertThat(clamped).isEqualTo(Material.DIAMOND_HELMET);
        }
    }
}
