package com.monkey.mcbot.sdk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monkey.mcbot.sdk.event.BotEventEnvelope;
import com.monkey.mcbot.sdk.event.SdkBotEventType;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class MinecraftBotEventBusTest {

    @Test
    void receivesFilteredSseEnvelope() throws Exception {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        UUID ownerUUID = UUID.randomUUID();
        UUID botUUID = UUID.randomUUID();
        BotEventEnvelope envelope = new BotEventEnvelope(
                7L, 1, "SPAWNED", UUID.randomUUID(), 1L, Instant.now(),
                ownerUUID, botUUID, "API", null, Map.of("mode", "EVENT"));
        AtomicReference<String> query = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/mcbot/api/v1/events", exchange -> {
            query.set(exchange.getRequestURI().getQuery());
            byte[] body = ("id: 7\nevent: SPAWNED\ndata: "
                    + mapper.writeValueAsString(envelope) + "\n\n").getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/event-stream");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();

        CountDownLatch received = new CountDownLatch(1);
        AtomicReference<BotEventEnvelope> actual = new AtomicReference<>();
        try (MinecraftBotClient client = MinecraftBotClient.builder()
                .baseUri("http://127.0.0.1:" + server.getAddress().getPort() + "/mcbot/api/v1/")
                .token("secret")
                .build()) {
            client.events().subscribeForOwner(ownerUUID, Set.of(SdkBotEventType.SPAWNED), event -> {
                actual.set(event);
                received.countDown();
            });
            assertTrue(received.await(5, TimeUnit.SECONDS));
            assertEquals(botUUID, actual.get().botUUID());
            assertEquals("EVENT", actual.get().payload().get("mode"));
            assertNotNull(query.get());
            assertTrue(query.get().contains("ownerUUID=" + ownerUUID));
            assertTrue(query.get().contains("types=SPAWNED"));
        } finally {
            server.stop(0);
        }
    }
}
