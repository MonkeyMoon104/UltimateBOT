package com.monkey.ultimatebot.access;

import static org.assertj.core.api.Assertions.assertThat;

import com.monkey.ultimatebot.access.item.MaterialAirAccess;
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
