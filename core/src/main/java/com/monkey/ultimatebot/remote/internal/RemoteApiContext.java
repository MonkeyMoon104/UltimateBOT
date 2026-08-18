package com.monkey.ultimatebot.remote.internal;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.api.UltimateBotAPI;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.util.concurrent.Callable;

public interface RemoteApiContext {
    UltimateBot plugin();

    UltimateBotAPI api();

    <T> T runSync(Callable<T> callable);

    <T> T readJson(HttpExchange exchange, Class<T> type) throws IOException;

    void writeJson(HttpExchange exchange, int status, Object body) throws IOException;

    void writeText(HttpExchange exchange, int status, String body) throws IOException;

    void handleEvents(HttpExchange exchange) throws IOException;

    void handleSpawn(HttpExchange exchange) throws IOException;

    void handleBotMutation(HttpExchange exchange, String method, String relativePath) throws IOException;
}
