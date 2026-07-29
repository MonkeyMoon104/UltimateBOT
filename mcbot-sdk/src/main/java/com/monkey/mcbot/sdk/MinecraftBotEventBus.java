package com.monkey.mcbot.sdk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monkey.mcbot.sdk.event.BotEventEnvelope;
import com.monkey.mcbot.sdk.event.BotEventSubscription;
import com.monkey.mcbot.sdk.event.SdkBotEventType;
import org.jspecify.annotations.Nullable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/** Reconnecting Server-Sent Events client for MinecraftBot remote events. */
public final class MinecraftBotEventBus implements AutoCloseable {
    private final URI baseUri;
    private final String token;
    private final HttpClient client;
    private final ObjectMapper mapper;
    private final Set<Subscription> subscriptions = ConcurrentHashMap.newKeySet();

    MinecraftBotEventBus(URI baseUri, String token, HttpClient client, ObjectMapper mapper) {
        this.baseUri = Objects.requireNonNull(baseUri, "baseUri");
        this.token = Objects.requireNonNull(token, "token");
        this.client = Objects.requireNonNull(client, "client");
        this.mapper = Objects.requireNonNull(mapper, "mapper");
    }

    public BotEventSubscription subscribe(Consumer<BotEventEnvelope> listener) {
        return subscribe(Set.of(), listener);
    }

    public BotEventSubscription subscribe(Set<SdkBotEventType> types, Consumer<BotEventEnvelope> listener) {
        Objects.requireNonNull(types, "types");
        Objects.requireNonNull(listener, "listener");
        Subscription subscription = new Subscription(Set.copyOf(types), null, null, listener);
        subscriptions.add(subscription);
        subscription.start();
        return subscription;
    }

    public BotEventSubscription subscribeForOwner(UUID ownerUUID, Set<SdkBotEventType> types,
                                                   Consumer<BotEventEnvelope> listener) {
        Subscription subscription = new Subscription(Set.copyOf(types),
                Objects.requireNonNull(ownerUUID, "ownerUUID"), null, Objects.requireNonNull(listener, "listener"));
        subscriptions.add(subscription);
        subscription.start();
        return subscription;
    }

    public BotEventSubscription subscribeForBot(UUID botUUID, Set<SdkBotEventType> types,
                                                 Consumer<BotEventEnvelope> listener) {
        Subscription subscription = new Subscription(Set.copyOf(types), null,
                Objects.requireNonNull(botUUID, "botUUID"), Objects.requireNonNull(listener, "listener"));
        subscriptions.add(subscription);
        subscription.start();
        return subscription;
    }

    @Override
    public void close() {
        for (Subscription subscription : List.copyOf(subscriptions)) subscription.close();
    }

    private final class Subscription implements BotEventSubscription {
        private final Set<SdkBotEventType> types;
        private final Consumer<BotEventEnvelope> listener;
        private final @Nullable UUID ownerUUID;
        private final @Nullable UUID botUUID;
        private final AtomicBoolean active = new AtomicBoolean(true);
        private final AtomicLong lastEventId = new AtomicLong();
        private volatile @Nullable Thread worker;

        private Subscription(Set<SdkBotEventType> types, @Nullable UUID ownerUUID, @Nullable UUID botUUID,
                             Consumer<BotEventEnvelope> listener) {
            this.types = Objects.requireNonNull(types, "types");
            this.ownerUUID = ownerUUID;
            this.botUUID = botUUID;
            this.listener = Objects.requireNonNull(listener, "listener");
        }

        private void start() {
            worker = Thread.startVirtualThread(this::run);
        }

        private void run() {
            long retryMillis = 500L;
            while (active.get()) {
                try {
                    consume();
                    retryMillis = 500L;
                } catch (Exception failure) {
                    if (!active.get()) return;
                    try {
                        Thread.sleep(retryMillis);
                    } catch (InterruptedException interrupted) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                    retryMillis = Math.min(30_000L, retryMillis * 2L);
                }
            }
        }

        private void consume() throws Exception {
            HttpRequest.Builder request = HttpRequest.newBuilder(eventsUri())
                    .timeout(Duration.ofMinutes(30))
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "text/event-stream")
                    .GET();
            if (lastEventId.get() > 0L) request.header("Last-Event-ID", Long.toString(lastEventId.get()));
            HttpResponse<java.io.InputStream> response = client.send(request.build(), HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                response.body().close();
                throw new MinecraftBotClientException("Event stream returned HTTP " + response.statusCode());
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
                String line;
                while (active.get() && (line = reader.readLine()) != null) {
                    if (!line.startsWith("data:")) continue;
                    BotEventEnvelope event = mapper.readValue(line.substring(5).trim(), BotEventEnvelope.class);
                    lastEventId.set(event.id());
                    try { listener.accept(event); } catch (RuntimeException ignored) { }
                }
            }
        }

        private URI eventsUri() {
            List<String> filters = new ArrayList<>();
            if (!types.isEmpty()) {
                String joined = types.stream().map(Enum::name).sorted().reduce((a, b) -> a + "," + b).orElse("");
                filters.add("types=" + URLEncoder.encode(joined, StandardCharsets.UTF_8));
            }
            if (ownerUUID != null) filters.add("ownerUUID=" + ownerUUID);
            if (botUUID != null) filters.add("botUUID=" + botUUID);
            return baseUri.resolve("events" + (filters.isEmpty() ? "" : "?" + String.join("&", filters)));
        }

        @Override public boolean isActive() { return active.get(); }
        @Override public long getLastEventId() { return lastEventId.get(); }
        @Override public void close() {
            if (active.compareAndSet(true, false)) {
                subscriptions.remove(this);
                Thread current = worker;
                if (current != null) current.interrupt();
            }
        }
    }
}
