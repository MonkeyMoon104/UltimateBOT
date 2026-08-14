package com.monkey.ultimatebot.compat;

import static org.assertj.core.api.Assertions.assertThat;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class MaterialAirAccessTest {

    @Test
    void airIsAir() {
        assertThat(MaterialAirAccess.isAir(Material.AIR)).isTrue();
    }

    @Test
    void stoneIsNotAir() {
        assertThat(MaterialAirAccess.isAir(Material.STONE)).isFalse();
    }
}
