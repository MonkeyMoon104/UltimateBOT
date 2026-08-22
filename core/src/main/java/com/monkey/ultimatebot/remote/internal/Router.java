package com.monkey.ultimatebot.remote.internal;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;

public final class Router {
    private final RequestHandlers handlers;

    public Router(RequestHandlers handlers) {
        this.handlers = handlers;
    }

    public boolean route(HttpExchange exchange, String method, String relativePath) throws IOException {
        if ("GET".equals(method) && "/health".equals(relativePath)) {
            handlers.handleHealth(exchange);
            return true;
        }
        if ("GET".equals(method) && "/platform".equals(relativePath)) {
            handlers.handlePlatform(exchange);
            return true;
        }
        if ("GET".equals(method) && "/settings".equals(relativePath)) {
            handlers.handleSettings(exchange);
            return true;
        }
        if ("GET".equals(method) && "/metrics".equals(relativePath)) {
            handlers.handleMetrics(exchange);
            return true;
        }
        if ("GET".equals(method) && "/events".equals(relativePath)) {
            handlers.handleEvents(exchange);
            return true;
        }
        if ("GET".equals(method) && "/bots".equals(relativePath)) {
            handlers.handleListBots(exchange);
            return true;
        }
        if ("GET".equals(method) && "/combat-modes".equals(relativePath)) {
            handlers.handleCombatModes(exchange);
            return true;
        }
        if ("GET".equals(method) && "/brains".equals(relativePath)) {
            handlers.handleBrains(exchange);
            return true;
        }
        if ("GET".equals(method) && "/addons".equals(relativePath)) {
            handlers.handleAddons(exchange);
            return true;
        }
        if ("GET".equals(method) && relativePath.startsWith("/brains/")) {
            handlers.handleBrain(exchange, relativePath);
            return true;
        }
        if ("GET".equals(method) && relativePath.startsWith("/combat-modes/")) {
            handlers.handleCombatMode(exchange, relativePath);
            return true;
        }
        if ("GET".equals(method) && "/bots/count".equals(relativePath)) {
            handlers.handleBotCount(exchange);
            return true;
        }
        if ("POST".equals(method) && "/bots".equals(relativePath)) {
            handlers.handleSpawn(exchange);
            return true;
        }
        if ("DELETE".equals(method) && "/bots".equals(relativePath)) {
            handlers.handleRemoveAllBots(exchange);
            return true;
        }
        if ("DELETE".equals(method) && relativePath.startsWith("/bots/source/")) {
            handlers.handleRemoveBotsBySource(exchange, relativePath);
            return true;
        }
        if (relativePath.startsWith("/bots/")) {
            handlers.handleBotMutation(exchange, method, relativePath);
            return true;
        }
        return false;
    }
}
