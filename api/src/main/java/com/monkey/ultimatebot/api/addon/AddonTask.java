package com.monkey.ultimatebot.api.addon;

/** Cancellable scheduler handle owned by an addon resource scope. */
public interface AddonTask extends AutoCloseable {
    /** Cancels this task. Repeated calls have no effect. */
    void cancel();

    /** Returns whether cancellation was requested. */
    boolean isCancelled();

    /** Returns whether this task completed or was cancelled. */
    boolean isDone();

    @Override
    default void close() {
        cancel();
    }
}
