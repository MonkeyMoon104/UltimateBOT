package com.monkey.ultimatebot.metrics.addon;

import static org.assertj.core.api.Assertions.assertThat;

import com.monkey.ultimatebot.common.metrics.MetricsBackend;
import com.monkey.ultimatebot.common.metrics.MetricsBackendContext;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class MicrometerMetricsBackendTest {
    @Test
    void exposesRuntimeEventCacheAndHttpMetrics() {
        MetricsBackendContext context =
                new MetricsBackendContext(true, "2.0.0", "1.21.4", () -> 2, () -> 3L, () -> 5L, () -> 7L, () -> 11L);

        try (MetricsBackend metrics = new MicrometerMetricsBackendFactory().create(context)) {
            metrics.recordEvent("TestBotEvent", false);
            metrics.recordObserverFailure();
            metrics.recordRemoteRequest("GET", "/health", 200, TimeUnit.MILLISECONDS.toNanos(12));

            assertThat(metrics.scrape())
                    .contains(
                            "ultimatebot_bots_active",
                            "ultimatebot_cache_target_hits_total",
                            "ultimatebot_events_published_total",
                            "ultimatebot_events_observer_failures_total",
                            "ultimatebot_remote_requests_seconds_count",
                            "route=\"/health\"",
                            "status=\"200\"");
        }
    }
}
