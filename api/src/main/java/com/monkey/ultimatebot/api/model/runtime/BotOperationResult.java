package com.monkey.ultimatebot.api.model.runtime;

import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * Standard result object for bot operations.
 *
 * @param success {@code true} when the operation completed successfully
 * @param message human-readable operation message (success or failure reason)
 * @param snapshot optional bot snapshot associated with the operation
 */
public final class BotOperationResult {
    private final boolean success;
    private final String message;
    private final @Nullable BotSnapshot snapshot;

    public BotOperationResult(boolean success, String message, @Nullable BotSnapshot snapshot) {
        this.success = success;
        this.message = message;
        this.snapshot = snapshot;
    }

    public boolean success() {
        return success;
    }

    public String message() {
        return message;
    }

    public @Nullable BotSnapshot snapshot() {
        return snapshot;
    }

    /**
     * Creates a successful operation result.
     *
     * @param message success message
     * @param snapshot bot snapshot after operation, may be {@code null}
     * @return success result
     */
    public static BotOperationResult success(String message, @Nullable BotSnapshot snapshot) {
        return new BotOperationResult(true, message, snapshot);
    }

    /**
     * Creates a failed operation result.
     *
     * @param message failure reason
     * @return failure result
     */
    public static BotOperationResult failure(String message) {
        return new BotOperationResult(false, message, null);
    }

    /**
     * Returns snapshot as an optional.
     *
     * @return optional snapshot
     */
    public Optional<BotSnapshot> snapshotOptional() {
        return Optional.ofNullable(snapshot);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BotOperationResult)) {
            return false;
        }
        BotOperationResult other = (BotOperationResult) obj;
        return success == other.success
                && java.util.Objects.equals(message, other.message)
                && java.util.Objects.equals(snapshot, other.snapshot);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(success, message, snapshot);
    }

    @Override
    public String toString() {
        return "BotOperationResult[success=" + success + ", message=" + message + ", snapshot=" + snapshot + "]";
    }
}
