package com.monkey.mcbot.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import com.monkey.mcbot.api.event.base.BotEvent;
import com.monkey.mcbot.bot.BotRegistry;
import com.monkey.mcbot.bot.ai.services.TargetingService;
import com.monkey.mcbot.config.RuntimeSettings;
import java.nio.file.Path;
import java.util.UUID;
import java.util.logging.Logger;
import org.bukkit.event.HandlerList;
import org.junit.jupiter.api.Test;

class BotMetricsTest {
    @Test
    void keepsPrometheusEndpointClosedWhenDisabled() {
        try (BotMetrics metrics = new BotMetrics(
                false,
                true,
                "1.3.2",
                "1.21.4",
                new BotRegistry(),
                new TargetingService(RuntimeSettings.defaults().targetCache()),
                Path.of("unused"),
                getClass().getClassLoader(),
                Logger.getAnonymousLogger())) {
            assertThat(metrics.isEnabled()).isFalse();
            assertThat(metrics.isPrometheusEndpointEnabled()).isFalse();
            assertThat(metrics.scrape()).isEmpty();
            metrics.recordEvent(new TestBotEvent());
            metrics.recordObserverFailure();
            metrics.recordRemoteRequest("GET", "/health", 200, 1L);
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
