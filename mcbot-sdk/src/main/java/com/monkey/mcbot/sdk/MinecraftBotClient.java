package com.monkey.mcbot.sdk;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monkey.mcbot.sdk.model.BotOperationResponse;
import com.monkey.mcbot.sdk.model.BotSnapshotResponse;
import com.monkey.mcbot.sdk.model.EventBotSpawnRequest;
import com.monkey.mcbot.sdk.model.ToggleRequest;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Java-only remote client for MinecraftBot servers with remote API enabled.
 *
 * <p>This SDK is safe to use with {@code implementation}; it does not depend on Bukkit,
 * Paper or the in-server {@code mcbot-api} classes.</p>
 */
public final class MinecraftBotClient implements AutoCloseable {

    private static final TypeReference<List<BotSnapshotResponse>> BOT_LIST_TYPE = new TypeReference<>() {
    };

    private final URI baseUri;
    private final String token;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private MinecraftBotClient(Builder builder) {
        this.baseUri = normalizeBaseUri(builder.baseUri);
        this.token = Objects.requireNonNull(builder.token, "token");
        this.objectMapper = builder.objectMapper == null
                ? new ObjectMapper()
                .findAndRegisterModules()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                : builder.objectMapper;
        this.httpClient = builder.httpClient == null
                ? HttpClient.newBuilder().connectTimeout(builder.timeout).build()
                : builder.httpClient;
    }

    public static Builder builder() {
        return new Builder();
    }

    public BotOperationResponse health() {
        return send("GET", "/health", null, BotOperationResponse.class);
    }

    public List<BotSnapshotResponse> listBots() {
        return send("GET", "/bots", null, BOT_LIST_TYPE);
    }

    public BotSnapshotResponse getBot(UUID ownerOrBotUUID) {
        return send("GET", "/bots/" + Objects.requireNonNull(ownerOrBotUUID, "ownerOrBotUUID"), null, BotSnapshotResponse.class);
    }

    public BotOperationResponse spawnEventBot(EventBotSpawnRequest request) {
        return send("POST", "/bots/event", Objects.requireNonNull(request, "request"), BotOperationResponse.class);
    }

    public BotOperationResponse remove(UUID ownerUUID) {
        return send("DELETE", "/bots/" + Objects.requireNonNull(ownerUUID, "ownerUUID"), null, BotOperationResponse.class);
    }

    public BotOperationResponse removeByBotUUID(UUID botUUID) {
        return send("DELETE", "/bots/" + Objects.requireNonNull(botUUID, "botUUID"), null, BotOperationResponse.class);
    }

    public BotOperationResponse removeAll() {
        return send("DELETE", "/bots", null, BotOperationResponse.class);
    }

    public BotOperationResponse updateCrystalPvp(UUID ownerUUID, boolean enabled) {
        return toggle(ownerUUID, "crystal-pvp", enabled);
    }

    public BotOperationResponse updateCrystalPvpByBotUUID(UUID botUUID, boolean enabled) {
        return toggle(Objects.requireNonNull(botUUID, "botUUID"), "crystal-pvp", enabled);
    }

    public BotOperationResponse updateExplosions(UUID ownerUUID, boolean enabled) {
        return toggle(ownerUUID, "explosions", enabled);
    }

    public BotOperationResponse updateExplosionsByBotUUID(UUID botUUID, boolean enabled) {
        return toggle(Objects.requireNonNull(botUUID, "botUUID"), "explosions", enabled);
    }

    public BotOperationResponse updateEnderPearls(UUID ownerUUID, boolean enabled) {
        return toggle(ownerUUID, "ender-pearls", enabled);
    }

    public BotOperationResponse updateEnderPearlsByBotUUID(UUID botUUID, boolean enabled) {
        return toggle(Objects.requireNonNull(botUUID, "botUUID"), "ender-pearls", enabled);
    }

    public BotOperationResponse updateAttackBots(UUID ownerUUID, boolean enabled) {
        return toggle(ownerUUID, "attack-bots", enabled);
    }

    public BotOperationResponse updateAttackBotsByBotUUID(UUID botUUID, boolean enabled) {
        return toggle(Objects.requireNonNull(botUUID, "botUUID"), "attack-bots", enabled);
    }

    private BotOperationResponse toggle(UUID ownerUUID, String field, boolean enabled) {
        Objects.requireNonNull(ownerUUID, "ownerUUID");
        return send("PATCH", "/bots/" + ownerUUID + "/" + field, new ToggleRequest(enabled), BotOperationResponse.class);
    }

    private <T> T send(String method, String path, Object body, Class<T> responseType) {
        try {
            HttpResponse<String> response = httpClient.send(buildRequest(method, path, body), HttpResponse.BodyHandlers.ofString());
            return decodeResponse(response, responseType);
        } catch (IOException ex) {
            throw new MinecraftBotClientException("Remote API request failed: " + ex.getMessage(), ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new MinecraftBotClientException("Remote API request interrupted", ex);
        }
    }

    private <T> T send(String method, String path, Object body, TypeReference<T> responseType) {
        try {
            HttpResponse<String> response = httpClient.send(buildRequest(method, path, body), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new MinecraftBotClientException("Remote API returned HTTP " + response.statusCode() + ": " + response.body());
            }
            return objectMapper.readValue(response.body(), responseType);
        } catch (IOException ex) {
            throw new MinecraftBotClientException("Remote API request failed: " + ex.getMessage(), ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new MinecraftBotClientException("Remote API request interrupted", ex);
        }
    }

    private HttpRequest buildRequest(String method, String path, Object body) throws IOException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(baseUri.resolve(trimLeadingSlash(path)))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json");

        if (body == null) {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        } else {
            builder.header("Content-Type", "application/json");
            builder.method(method, HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)));
        }
        return builder.build();
    }

    private <T> T decodeResponse(HttpResponse<String> response, Class<T> responseType) throws IOException {
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new MinecraftBotClientException("Remote API returned HTTP " + response.statusCode() + ": " + response.body());
        }
        return objectMapper.readValue(response.body(), responseType);
    }

    private static URI normalizeBaseUri(URI baseUri) {
        Objects.requireNonNull(baseUri, "baseUri");
        String raw = baseUri.toString();
        return URI.create(raw.endsWith("/") ? raw : raw + "/");
    }

    private static String trimLeadingSlash(String path) {
        return path.startsWith("/") ? path.substring(1) : path;
    }

    @Override
    public void close() {
        httpClient.close();
    }

    public static final class Builder {
        private URI baseUri = URI.create("http://127.0.0.1:8765/mcbot/api/v1/");
        private String token;
        private Duration timeout = Duration.ofSeconds(5);
        private HttpClient httpClient;
        private ObjectMapper objectMapper;

        private Builder() {
        }

        public Builder baseUri(String baseUri) {
            this.baseUri = URI.create(Objects.requireNonNull(baseUri, "baseUri"));
            return this;
        }

        public Builder baseUri(URI baseUri) {
            this.baseUri = Objects.requireNonNull(baseUri, "baseUri");
            return this;
        }

        public Builder token(String token) {
            this.token = Objects.requireNonNull(token, "token");
            return this;
        }

        public Builder timeout(Duration timeout) {
            this.timeout = Objects.requireNonNull(timeout, "timeout");
            return this;
        }

        public Builder httpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        public Builder objectMapper(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
            return this;
        }

        public MinecraftBotClient build() {
            if (token == null || token.isBlank()) {
                throw new IllegalArgumentException("token is required");
            }
            return new MinecraftBotClient(this);
        }
    }
}
