package com.monkey.mcbot.api.model;

import java.util.Optional;

public record BotOperationResult(
        boolean success,
        String message,
        BotSnapshot snapshot
) {
    public static BotOperationResult success(String message, BotSnapshot snapshot) {
        return new BotOperationResult(true, message, snapshot);
    }

    public static BotOperationResult failure(String message) {
        return new BotOperationResult(false, message, null);
    }

    public Optional<BotSnapshot> snapshotOptional() {
        return Optional.ofNullable(snapshot);
    }
}
