package com.monkey.ultimatebot.sdk.model.request;

import java.util.Objects;

/** Runtime request for enabling and replacing a bot kill message. */
public final class KillMessageRequest {
    private final String message;

    public KillMessageRequest(String message) {

        Objects.requireNonNull(message, "message");
        if (message.trim().isEmpty()) {
            throw new IllegalArgumentException("message cannot be blank");
        }
        this.message = message;
    }

    public String message() {
        return message;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof KillMessageRequest)) {
            return false;
        }
        KillMessageRequest other = (KillMessageRequest) obj;
        return java.util.Objects.equals(message, other.message);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(message);
    }

    @Override
    public String toString() {
        return "KillMessageRequest[message=" + message + "]";
    }
}
