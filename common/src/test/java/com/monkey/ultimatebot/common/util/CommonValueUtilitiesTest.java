package com.monkey.ultimatebot.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.monkey.ultimatebot.common.model.BotMode;
import org.junit.jupiter.api.Test;

class CommonValueUtilitiesTest {
    @Test
    void parsesExternalEnumValuesWithoutLeakingParsingPolicy() {
        assertThat(EnumValues.parse(BotMode.class, " team_ally ", BotMode.SINGLE))
                .isEqualTo(BotMode.TEAM_ALLY);
        assertThat(EnumValues.parse(BotMode.class, "invalid", BotMode.SINGLE)).isEqualTo(BotMode.SINGLE);
    }

    @Test
    void normalizesNullableTextAtModuleBoundaries() {
        assertThat(TextValues.trimToNull("  ")).isNull();
        assertThat(TextValues.trimToNull(" bot ")).isEqualTo("bot");
        assertThat(TextValues.orElseIfBlank(null, "fallback")).isEqualTo("fallback");
    }
}
