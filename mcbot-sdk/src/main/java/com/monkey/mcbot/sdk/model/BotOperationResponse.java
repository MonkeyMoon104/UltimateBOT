package com.monkey.mcbot.sdk.model;

import org.jspecify.annotations.Nullable;

/**
 * Standard response returned by remote API operations.
 *
 * @param success whether the requested operation completed successfully
 * @param message human-readable server result or error message
 * @param snapshot latest bot state when the operation targets a single bot
 * @param removedCount number of removed bots for bulk deletion operations
 */
public record BotOperationResponse(
        boolean success,
        @Nullable String message,
        @Nullable BotSnapshotResponse snapshot,
        @Nullable Integer removedCount) {
    /**
     * Returns {@code true} when the response contains a bot snapshot.
     *
     * @return whether a snapshot is available
     */
    public boolean hasSnapshot() {
        return snapshot != null;
    }

    /**
     * Returns the removal count, using zero when the server did not return one.
     *
     * @return safe removal count
     */
    public int removedCountOrZero() {
        return removedCount == null ? 0 : removedCount;
    }

    /**
     * Throws when the operation failed and otherwise returns this response.
     *
     * @return this response
     */
    public BotOperationResponse requireSuccess() {
        if (!success) {
            throw new IllegalStateException(
                    message == null || message.isBlank() ? "Remote API operation failed" : message);
        }
        return this;
    }
}
