package com.monkey.ultimatebot.combat.mode.shared;

import static org.assertj.core.api.Assertions.assertThat;

import com.monkey.ultimatebot.common.model.CombatMode;
import com.monkey.ultimatebot.utils.armor.ArmorTier;
import org.junit.jupiter.api.Test;

class CombatModeLoadoutDefaultsTest {
    @Test
    void assignsArmorThatMatchesEachModeTemplate() {
        assertThat(CombatModeLoadoutDefaults.armorTier(CombatMode.SWORD)).isEqualTo(ArmorTier.DIAMOND);
        assertThat(CombatModeLoadoutDefaults.armorTier(CombatMode.UHC)).isEqualTo(ArmorTier.DIAMOND);
        assertThat(CombatModeLoadoutDefaults.armorTier(CombatMode.WATER)).isEqualTo(ArmorTier.DIAMOND);
        assertThat(CombatModeLoadoutDefaults.armorTier(CombatMode.TRIDENT)).isEqualTo(ArmorTier.DIAMOND);

        ArmorTier highTier = ArmorTier.maxAvailable();
        assertThat(CombatModeLoadoutDefaults.armorTier(CombatMode.CART)).isEqualTo(highTier);
        assertThat(CombatModeLoadoutDefaults.armorTier(CombatMode.CRYSTAL)).isEqualTo(highTier);
        assertThat(CombatModeLoadoutDefaults.armorTier(CombatMode.MACE)).isEqualTo(highTier);
        assertThat(CombatModeLoadoutDefaults.armorTier(CombatMode.AXE_SHIELD)).isEqualTo(highTier);
        assertThat(CombatModeLoadoutDefaults.armorTier(CombatMode.NETHERITE_POT)).isEqualTo(highTier);
        assertThat(CombatModeLoadoutDefaults.armorTier(CombatMode.SMP)).isEqualTo(highTier);
    }
}
