package com.monkey.ultimatebot.combat.profile;

import static org.assertj.core.api.Assertions.assertThat;

import com.monkey.ultimatebot.api.model.combat.CombatMode;
import com.monkey.ultimatebot.api.model.combat.CombatTuning;
import com.monkey.ultimatebot.api.model.combat.DifficultyTier;
import com.monkey.ultimatebot.api.model.platform.PlatformCapability;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CombatProfileCatalogCapabilityFilterTest {
    @Test
    void enabledModesIntersectConfigAndPlatformCapabilities() {
        CombatProfileCatalog catalog =
                new CombatProfileCatalog(allEnabledConfigurations(), EnumSet.allOf(PlatformCapability.class));
        assertThat(catalog.enabledModes()).contains(CombatMode.MACE, CombatMode.CART);

        catalog.bindPlatformCapabilities(
                EnumSet.complementOf(EnumSet.of(PlatformCapability.MACE, PlatformCapability.WIND_CHARGE)));
        assertThat(catalog.enabledModes()).doesNotContain(CombatMode.MACE);
        assertThat(catalog.platformDisabledModes()).contains(CombatMode.MACE);
        assertThat(catalog.enabledModes()).contains(CombatMode.CART, CombatMode.SWORD);
    }

    private static Map<CombatMode, CombatModeConfiguration> allEnabledConfigurations() {
        Map<CombatMode, CombatModeConfiguration> configurations = new LinkedHashMap<>();
        EnumMap<DifficultyTier, CombatTuning> profiles = new EnumMap<>(DifficultyTier.class);
        for (DifficultyTier difficulty : DifficultyTier.values()) {
            profiles.put(difficulty, CombatTuning.builder().build());
        }
        Map<DifficultyTier, CombatTuning> profileMap =
                com.monkey.ultimatebot.api.util.ImmutableCollections.copyOf(profiles);
        for (CombatMode mode : CombatMode.values()) {
            configurations.put(mode, new CombatModeConfiguration(mode, true, "IRON_SWORD", profileMap));
        }
        return configurations;
    }
}
