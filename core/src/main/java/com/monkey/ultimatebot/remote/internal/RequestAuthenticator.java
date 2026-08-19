package com.monkey.ultimatebot.remote.internal;

import com.sun.net.httpserver.HttpExchange;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Supplier;

public final class RequestAuthenticator {
    private final Supplier<String> tokenSupplier;

    public RequestAuthenticator(Supplier<String> tokenSupplier) {
        this.tokenSupplier = Objects.requireNonNull(tokenSupplier, "tokenSupplier");
    }

    public boolean isAuthorized(HttpExchange exchange) {
        String token = tokenSupplier.get();
        String authorization = exchange.getRequestHeaders().getFirst("Authorization");
        boolean authorizationMatch = constantTimeEquals(authorization, "Bearer " + token);
        String headerToken = exchange.getRequestHeaders().getFirst("X-UltimateBot-Token");
        boolean headerMatch = constantTimeEquals(headerToken, token);
        return authorizationMatch || headerMatch;
    }

    private static boolean constantTimeEquals(String left, String right) {
        if (left == null || right == null) {
            return false;
        }
        byte[] leftBytes = left.getBytes(StandardCharsets.UTF_8);
        byte[] rightBytes = right.getBytes(StandardCharsets.UTF_8);
        int maxLength = Math.max(leftBytes.length, rightBytes.length);
        int diff = leftBytes.length ^ rightBytes.length;
        for (int i = 0; i < maxLength; i++) {
            byte leftByte = i < leftBytes.length ? leftBytes[i] : 0;
            byte rightByte = i < rightBytes.length ? rightBytes[i] : 0;
            diff |= leftByte ^ rightByte;
        }
        return diff == 0;
    }
}
