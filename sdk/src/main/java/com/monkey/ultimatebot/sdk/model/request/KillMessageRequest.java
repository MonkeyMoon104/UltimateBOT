package com.monkey.ultimatebot.sdk.model.request;

import java.util.Objects;

/** Runtime request for enabling and replacing a bot kill message. */
public record KillMessageRequest(String message) {
    public KillMessageRequest {
        Objects.requireNonNull(message, "message");
        if (message.isBlank()) {
            throw new IllegalArgumentException("message cannot be blank");
        }
    }
}
