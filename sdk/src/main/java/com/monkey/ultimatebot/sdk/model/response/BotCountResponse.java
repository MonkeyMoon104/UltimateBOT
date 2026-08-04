package com.monkey.ultimatebot.sdk.model.response;

/** Number of active bots currently tracked by the server. */
public record BotCountResponse(int count) {
    public BotCountResponse {
        if (count < 0) {
            throw new IllegalArgumentException("count cannot be negative");
        }
    }
}
