package com.monkey.mcbot.api.model;

import java.util.Optional;

/**
 * Standard result object for bot operations.
 *
 * @param success {@code true} when the operation completed successfully
 * @param message human-readable operation message (success or failure reason)
 * @param snapshot optional bot snapshot associated with the operation
 */
public record BotOperationResult(
        boolean success,
        String message,
        BotSnapshot snapshot
) {
    /**
     * Creates a successful operation result.
     *
     * @param message success message
     * @param snapshot bot snapshot after operation, may be {@code null}
     * @return success result
     */
    public static BotOperationResult success(String message, BotSnapshot snapshot) {
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
