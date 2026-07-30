package com.monkey.mcbot.metrics.addon;

import static org.assertj.core.api.Assertions.assertThat;

import com.monkey.mcbot.common.metrics.MetricsBackend;
import com.monkey.mcbot.common.metrics.MetricsBackendContext;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class MicrometerMetricsBackendTest {
    @Test
    void exposesRuntimeEventCacheAndHttpMetrics() {
        MetricsBackendContext context =
                new MetricsBackendContext(true, "1.4.1", "1.21.4", () -> 2, () -> 3L, () -> 5L, () -> 7L, () -> 11L);

        try (MetricsBackend metrics = new MicrometerMetricsBackendFactory().create(context)) {
            metrics.recordEvent("TestBotEvent", false);
            metrics.recordObserverFailure();
            metrics.recordRemoteRequest("GET", "/health", 200, TimeUnit.MILLISECONDS.toNanos(12));

            assertThat(metrics.scrape())
                    .contains(
                            "minecraftbot_bots_active",
                            "minecraftbot_cache_target_hits_total",
                            "minecraftbot_events_published_total",
                            "minecraftbot_events_observer_failures_total",
                            "minecraftbot_remote_requests_seconds_count",
                            "route=\"/health\"",
                            "status=\"200\"");
        }
    }
}
