package com.monkey.ultimatebot.common.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MetricsBackendContextTest {
    @Test
    void exposesJavaOnlyRuntimeSuppliers() {
        MetricsBackendContext context =
                new MetricsBackendContext(true, "1.5.0", "1.21.4", () -> 1, () -> 2L, () -> 3L, () -> 4L, () -> 5L);

        assertThat(context.activeBots().getAsInt()).isEqualTo(1);
        assertThat(context.targetCacheSize().getAsLong()).isEqualTo(2L);
        assertThat(context.targetCacheHits().getAsLong()).isEqualTo(3L);
        assertThat(context.targetCacheMisses().getAsLong()).isEqualTo(4L);
        assertThat(context.targetCacheEvictions().getAsLong()).isEqualTo(5L);
    }
}
