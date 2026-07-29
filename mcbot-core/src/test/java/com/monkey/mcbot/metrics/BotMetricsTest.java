package com.monkey.mcbot.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import com.monkey.mcbot.api.event.base.BotEvent;
import com.monkey.mcbot.bot.BotRegistry;
import com.monkey.mcbot.bot.ai.services.TargetingService;
import com.monkey.mcbot.config.RuntimeSettings;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.bukkit.event.HandlerList;
import org.junit.jupiter.api.Test;

class BotMetricsTest {
    @Test
    void exposesRuntimeEventCacheAndHttpMetrics() {
        BotRegistry bots = new BotRegistry();
        TargetingService targeting =
                new TargetingService(RuntimeSettings.defaults().targetCache());

        try (BotMetrics metrics = new BotMetrics(true, true, "1.3.2", "1.21.4", bots, targeting)) {
            metrics.recordEvent(new TestBotEvent());
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

    @Test
    void keepsPrometheusEndpointClosedWhenDisabled() {
        try (BotMetrics metrics = new BotMetrics(
                false,
                true,
                "1.3.2",
                "1.21.4",
                new BotRegistry(),
                new TargetingService(RuntimeSettings.defaults().targetCache()))) {
            assertThat(metrics.isEnabled()).isFalse();
            assertThat(metrics.isPrometheusEndpointEnabled()).isFalse();
            assertThat(metrics.scrape()).isEmpty();
        }
    }

    private static final class TestBotEvent extends BotEvent {
        private static final HandlerList HANDLERS = new HandlerList();

        private TestBotEvent() {
            super(1L, UUID.randomUUID(), UUID.randomUUID(), null, null);
        }

        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }
    }
}
