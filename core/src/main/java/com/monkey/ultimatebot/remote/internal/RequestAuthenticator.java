package com.monkey.ultimatebot.remote.internal;

import com.sun.net.httpserver.HttpExchange;
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
        if (authorization != null && authorization.equals("Bearer " + token)) {
            return true;
        }
        String headerToken = exchange.getRequestHeaders().getFirst("X-UltimateBot-Token");
        return token.equals(headerToken);
    }
}
