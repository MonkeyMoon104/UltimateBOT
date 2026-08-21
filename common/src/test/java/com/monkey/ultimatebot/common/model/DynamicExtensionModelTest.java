package com.monkey.ultimatebot.common.model;

import com.monkey.ultimatebot.common.model.brain.BrainKey;
import com.monkey.ultimatebot.common.model.combat.CombatMode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class DynamicExtensionModelTest {
    @Test
    void parsesBuiltinAndNamespacedModeIdentifiers() {
        assertThat(CombatMode.parse("water")).isEqualTo(CombatMode.WATER);
        assertThat(CombatMode.parse("example:elytra-pvp")).isEqualTo(CombatMode.of("example", "elytra-pvp"));
        assertThat(CombatMode.of("example", "elytra-pvp").builtIn()).isFalse();
        assertThat(CombatMode.of("example", "sword").capabilities()).isEmpty();
        assertThat(CombatMode.of("example", "sword").displayName()).isEqualTo("Sword");
    }

    @Test
    void validatesBrainAndModeKeysAtTheBoundary() {
        assertThat(BrainKey.parse("example:expert-ai").key()).isEqualTo("example:expert-ai");
        assertThatThrownBy(() -> BrainKey.parse("missing_namespace")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> CombatMode.of("Example Invalid", "mode")).isInstanceOf(IllegalArgumentException.class);
    }
}
