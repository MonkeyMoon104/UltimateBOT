package com.monkey.ultimatebot.remote.internal;

import com.monkey.ultimatebot.api.model.runtime.BotOperationResult;
import com.monkey.ultimatebot.common.model.AddonInfo;
import com.monkey.ultimatebot.common.model.BotSource;
import com.monkey.ultimatebot.common.model.BrainKey;
import com.monkey.ultimatebot.common.model.CombatMode;
import com.monkey.ultimatebot.common.util.EnumValues;
import com.monkey.ultimatebot.common.util.ImmutableCollections;
import com.monkey.ultimatebot.metrics.BotMetrics;
import com.monkey.ultimatebot.remote.RemoteApiServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;

public final class RequestHandlers {
    private final RemoteApiContext context;
    private final ResponseWriter responseWriter;

    public RequestHandlers(RemoteApiContext context, ResponseWriter responseWriter) {
        this.context = context;
        this.responseWriter = responseWriter;
    }

    public void handleHealth(HttpExchange exchange) throws IOException {
        responseWriter.writeJson(
                exchange, 200, RemoteApiServer.RemoteOperationResponse.success("UltimateBot remote API is online.", null));
    }

    public void handlePlatform(HttpExchange exchange) throws IOException {
        responseWriter.writeJson(exchange, 200, context.api().getBotManager().getPlatform());
    }

    public void handleMetrics(HttpExchange exchange) throws IOException {
        BotMetrics metrics = context.plugin().getBotMetrics();
        if (!metrics.isPrometheusEndpointEnabled()) {
            responseWriter.writeJson(exchange, 404, RemoteApiServer.RemoteOperationResponse.failure("Metrics endpoint is disabled."));
            return;
        }
        responseWriter.writeText(exchange, 200, metrics.scrape());
    }

    public void handleEvents(HttpExchange exchange) throws IOException {
        context.handleEvents(exchange);
    }

    public void handleListBots(HttpExchange exchange) throws IOException {
        responseWriter.writeJson(exchange, 200, context.api().getBotRegistry().getAllBots().values());
    }

    public void handleCombatModes(HttpExchange exchange) throws IOException {
        responseWriter.writeJson(exchange, 200, context.api().getBotManager().getCombatModes());
    }

    public void handleBrains(HttpExchange exchange) throws IOException {
        responseWriter.writeJson(exchange, 200, context.api().getBotManager().getBrains());
    }

    public void handleAddons(HttpExchange exchange) throws IOException {
        responseWriter.writeJson(
                exchange,
                200,
                context.api().getAddons().addons().stream()
                        .map(snapshot -> new AddonInfo(
                                snapshot.descriptor().id(),
                                snapshot.descriptor().name(),
                                snapshot.descriptor().version(),
                                snapshot.state().name(),
                                snapshot.descriptor().authors(),
                                snapshot.descriptor().dependencies(),
                                snapshot.failure()))
                        .collect(Collectors.toList()));
    }

    public void handleBrain(HttpExchange exchange, String relativePath) throws IOException {
        String requestedBrain = relativePath.substring("/brains/".length());
        Optional<com.monkey.ultimatebot.common.model.BrainDefinition> definition;
        try {
            definition = context.api().getBotManager().getBrain(BrainKey.parse(requestedBrain));
        } catch (IllegalArgumentException exception) {
            definition = Optional.empty();
        }
        if (!definition.isPresent()) {
            responseWriter.writeJson(exchange, 404, RemoteApiServer.RemoteOperationResponse.failure("Brain not found."));
        } else {
            responseWriter.writeJson(exchange, 200, definition.get());
        }
    }

    public void handleCombatMode(HttpExchange exchange, String relativePath) throws IOException {
        String requestedMode = relativePath.substring("/combat-modes/".length());
        CombatMode combatMode = parseCombatMode(requestedMode);
        Optional<com.monkey.ultimatebot.common.model.CombatModeDefinition> definition = combatMode == null
                ? Optional.empty()
                : context.api().getBotManager().getCombatMode(combatMode);
        if (!definition.isPresent()) {
            responseWriter.writeJson(exchange, 404, RemoteApiServer.RemoteOperationResponse.failure("Combat mode not found."));
        } else {
            responseWriter.writeJson(exchange, 200, definition.get());
        }
    }

    public void handleBotCount(HttpExchange exchange) throws IOException {
        responseWriter.writeJson(
                exchange,
                200,
                ImmutableCollections.mapOf("count", context.api().getBotManager().getActiveBotCount()));
    }

    public void handleSpawn(HttpExchange exchange) throws IOException {
        context.handleSpawn(exchange);
    }

    public void handleRemoveAllBots(HttpExchange exchange) throws IOException {
        int removed = context.runSync(() -> context.api().getBotManager().removeAll());
        responseWriter.writeJson(exchange, 200, RemoteApiServer.RemoteOperationResponse.removed("Removed all bots.", removed));
    }

    public void handleRemoveBotsBySource(HttpExchange exchange, String relativePath) throws IOException {
        String requestedSource = relativePath.substring("/bots/source/".length());
        BotSource source = EnumValues.parse(BotSource.class, requestedSource, null);
        if (source == null) {
            responseWriter.writeJson(exchange, 400, RemoteApiServer.RemoteOperationResponse.failure("Invalid bot source."));
            return;
        }
        int removed = context.runSync(() -> context.api().getBotManager().removeBySource(source));
        responseWriter.writeJson(exchange, 200, RemoteApiServer.RemoteOperationResponse.removed("Removed bots by source.", removed));
    }

    public void handleBotMutation(HttpExchange exchange, String method, String relativePath) throws IOException {
        context.handleBotMutation(exchange, method, relativePath);
    }

    private static CombatMode parseCombatMode(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return CombatMode.parse(value);
    }
}
