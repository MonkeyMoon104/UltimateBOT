package com.monkey.ultimatebot.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.api.event.base.BotEvent;
import com.monkey.ultimatebot.metrics.BotMetrics;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

class BotEventDispatcherTest {

    @Test
    void publishesToBukkitAndRegisteredObservers() throws Exception {
        Server server = mock(Server.class);
        PluginManager pluginManager = mock(PluginManager.class);
        when(server.getPluginManager()).thenReturn(pluginManager);
        UltimateBot plugin = mock(UltimateBot.class);
        when(plugin.getServer()).thenReturn(server);
        when(plugin.getLogger()).thenReturn(Logger.getLogger(BotEventDispatcherTest.class.getName()));
        BotMetrics metrics = mock(BotMetrics.class);
        BotEventDispatcher dispatcher = new BotEventDispatcher(plugin, metrics);
        @SuppressWarnings("unchecked")
        Consumer<BotEvent> observer = mock(Consumer.class);
        TestBotEvent event = new TestBotEvent(dispatcher.nextSequence(UUID.randomUUID()));

        AutoCloseable registration = dispatcher.observe(observer);
        try {
            assertThat(dispatcher.publish(event)).isSameAs(event);
            verify(pluginManager).callEvent(event);
            verify(metrics).recordEvent(event);
            verify(observer).accept(event);
        } finally {
            registration.close();
        }
    }

    @Test
    void exposesMockBukkitHarnessOnTestClasspath() {
        assertThat(MockBukkit.class.getPackageName()).isEqualTo("org.mockbukkit.mockbukkit");
    }

    private static final class TestBotEvent extends BotEvent {
        private static final HandlerList HANDLERS = new HandlerList();

        private TestBotEvent(long sequence) {
            super(sequence, UUID.randomUUID(), UUID.randomUUID(), null, null);
        }

        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }
    }
}
