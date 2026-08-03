package com.monkey.ultimatebot.gui.combat;

import static org.assertj.core.api.Assertions.assertThat;

import com.monkey.ultimatebot.common.model.CombatTuning;
import org.junit.jupiter.api.Test;

class CombatTuningPropertyTest {

    @Test
    void everyPropertyProducesAValidatedProfile() {
        CombatTuning tuning = CombatTuning.builder().build();

        for (CombatTuningProperty property : CombatTuningProperty.values()) {
            tuning = property.adjust(tuning, true, false);
            assertThat(property.formattedValue(tuning)).isNotBlank();
        }
    }

    @Test
    void actionsPerTickIsClampedAtItsSafeMaximum() {
        CombatTuning tuning = CombatTuning.builder().maxActionsPerTick(8).build();

        CombatTuning adjusted = CombatTuningProperty.ACTIONS_PER_TICK.adjust(tuning, true, true);

        assertThat(adjusted.maxActionsPerTick()).isEqualTo(8);
    }
}
