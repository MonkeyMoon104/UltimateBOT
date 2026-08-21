package com.monkey.ultimatebot.common.model.platform;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PlatformInfoTest {

    @Test
    void exposesVersionAndCapabilityLookup() {
        PlatformInfo info = new PlatformInfo(
                "1.7.10",
                "1.7.10, 1.8.x-1.16.5",
                PlatformCapability.through1_8(),
                true);

        assertThat(info.minecraftVersion()).isEqualTo("1.7.10");
        assertThat(info.supports(PlatformCapability.TNT_MINECART)).isTrue();
        assertThat(info.supports(PlatformCapability.OFFHAND)).isFalse();
        assertThat(info.supports(PlatformCapability.TOTEM)).isFalse();
        assertThat(info.capabilities()).isUnmodifiable();
    }
}
