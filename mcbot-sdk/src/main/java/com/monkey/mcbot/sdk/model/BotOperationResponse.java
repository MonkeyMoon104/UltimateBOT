package com.monkey.mcbot.sdk.model;

public record BotOperationResponse(
        boolean success,
        String message,
        BotSnapshotResponse snapshot,
        Integer removedCount
) {
}
