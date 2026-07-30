package com.monkey.mcbot.common.metrics;

/** Receives runtime measurements without exposing a specific observability implementation. */
public interface MetricsBackend extends AutoCloseable {
    /** Backend used when observability is disabled or unavailable. */
    MetricsBackend NOOP = new MetricsBackend() {};

    /** Records a published bot event. */
    default void recordEvent(String eventType, boolean cancelled) {}

    /** Records an event observer failure. */
    default void recordObserverFailure() {}

    /** Records a remote API request. */
    default void recordRemoteRequest(String method, String route, int status, long durationNanos) {}

    /** Returns the current metrics exposition, or an empty string when unavailable. */
    default String scrape() {
        return "";
    }

    /** Releases resources owned by the backend. */
    @Override
    default void close() {}
}
