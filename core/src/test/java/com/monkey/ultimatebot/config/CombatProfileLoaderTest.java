package com.monkey.ultimatebot.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.monkey.ultimatebot.combat.profile.CombatProfileCatalog;
import com.monkey.ultimatebot.common.model.CombatMode;
import com.monkey.ultimatebot.common.model.DifficultyTier;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;

class CombatProfileLoaderTest {
    @Test
    void loadsEveryModeAndDifficultyFromBundledConfiguration() throws URISyntaxException {
        Path configurationPath = Path.of(
                java.util.Objects.requireNonNull(getClass().getResource("/combat-modes.yml"), "combat-modes.yml")
                        .toURI());

        CombatProfileCatalog catalog = new CombatProfileLoader(configurationPath, Logger.getAnonymousLogger()).load();

        assertThat(catalog.enabledModes()).containsExactly(CombatMode.values());
        assertThat(catalog.resolve(CombatMode.CRYSTAL, DifficultyTier.GOD).maxActionsPerTick())
                .isEqualTo(6);
        assertThat(catalog.resolve(CombatMode.UHC, DifficultyTier.EASY).specialActionCooldownTicks())
                .isEqualTo(100);
    }
}
