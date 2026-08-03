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
public record BotOperationResult(
        boolean success, String message, @Nullable BotSnapshot snapshot) {
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
}
