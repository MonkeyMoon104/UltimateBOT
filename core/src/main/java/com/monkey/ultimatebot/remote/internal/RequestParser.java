package com.monkey.ultimatebot.remote.internal;

import com.monkey.ultimatebot.libs.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.InputStream;

public final class RequestParser {
    private final ObjectMapper objectMapper;

    public RequestParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T> T readJson(HttpExchange exchange, Class<T> type) throws IOException {
        try (InputStream input = exchange.getRequestBody()) {
            return objectMapper.readValue(input, type);
        }
    }
}
