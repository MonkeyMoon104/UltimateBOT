package com.monkey.ultimatebot.sdk.model.response;

import org.jspecify.annotations.Nullable;

/**
 * Standard response returned by remote API operations.
 *
 * @param success whether the requested operation completed successfully
 * @param message human-readable server result or error message
 * @param snapshot latest bot state when the operation targets a single bot
 * @param removedCount number of removed bots for bulk deletion operations
 */
public final class BotOperationResponse {
    private final boolean success;
    private final String message;
    private final BotSnapshotResponse snapshot;
    private final Integer removedCount;

    public BotOperationResponse(boolean success, String message, BotSnapshotResponse snapshot, Integer removedCount) {
        this.success = success;
        this.message = message;
        this.snapshot = snapshot;
        this.removedCount = removedCount;
    }

    public boolean success() {
        return success;
    }
    public String message() {
        return message;
    }
    public BotSnapshotResponse snapshot() {
        return snapshot;
    }
    public Integer removedCount() {
        return removedCount;
    }

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
                    message == null || message.trim().isEmpty() ? "Remote API operation failed" : message);
        }
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BotOperationResponse)) {
            return false;
        }
        BotOperationResponse other = (BotOperationResponse) obj;
        return success == other.success && java.util.Objects.equals(message, other.message) && java.util.Objects.equals(snapshot, other.snapshot) && java.util.Objects.equals(removedCount, other.removedCount);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(success, message, snapshot, removedCount);
    }

    @Override
    public String toString() {
        return "BotOperationResponse[success=" + success + ", message=" + message + ", snapshot=" + snapshot + ", removedCount=" + removedCount + "]";
    }
}
