package com.monkey.ultimatebot.common.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.EnumMap;
import java.util.EnumSet;
import org.junit.jupiter.api.Test;

class CombatModeDefinitionTest {

    @Test
    void requiresAndExposesEveryDifficultyProfile() {
        EnumMap<DifficultyTier, CombatTuning> profiles = new EnumMap<>(DifficultyTier.class);
        for (DifficultyTier difficulty : DifficultyTier.values()) {
            profiles.put(difficulty, CombatTuning.builder().attackRange(4.0D).build());
        }
        CombatModeDefinition definition = new CombatModeDefinition(
                CombatMode.UHC,
                "UHC PvP",
                true,
                "GOLDEN_APPLE",
                EnumSet.copyOf(CombatMode.UHC.capabilities()),
                profiles);

        assertThat(definition.profiles())
                .hasSize(DifficultyTier.values().length)
                .isUnmodifiable();
        assertThat(definition.profile(DifficultyTier.GOD).attackRange()).isEqualTo(4.0D);
        assertThat(definition.requiredPlatformCapabilities()).isEmpty();
    }

    @Test
    void rejectsAnIncompleteProfileCatalog() {
        EnumMap<DifficultyTier, CombatTuning> incomplete = new EnumMap<>(DifficultyTier.class);
        incomplete.put(DifficultyTier.EASY, CombatTuning.builder().build());
        assertThatThrownBy(() -> new CombatModeDefinition(
                        CombatMode.SWORD,
                        "Sword PvP",
                        true,
                        "DIAMOND_SWORD",
                        CombatMode.SWORD.capabilities(),
                        incomplete))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("profiles");
    }
}
