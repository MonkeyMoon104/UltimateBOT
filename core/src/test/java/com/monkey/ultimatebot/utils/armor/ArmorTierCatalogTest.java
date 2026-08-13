package com.monkey.ultimatebot.utils.armor;

import static org.assertj.core.api.Assertions.assertThat;

import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.junit.jupiter.api.Test;

class ArmorTierCatalogTest {

    @Test
    void maxAvailableIsAtLeastDiamondWhenNetheriteExists() {
        assertThat(ArmorTier.maxAvailable()).isIn(ArmorTier.DIAMOND, ArmorTier.NETHERITE);
        if (Material.matchMaterial("NETHERITE_HELMET") != null) {
            assertThat(ArmorTier.maxAvailable()).isEqualTo(ArmorTier.NETHERITE);
        }
    }

    @Test
    void toMaterialResolvesViaCatalog() {
        Material helmet = ArmorTier.DIAMOND.toMaterial(EquipmentSlot.HEAD);
        assertThat(helmet).isEqualTo(Material.DIAMOND_HELMET);
    }
}
