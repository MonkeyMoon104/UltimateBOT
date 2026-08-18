package com.monkey.ultimatebot.common.metrics;

public interface MetricsBackend extends AutoCloseable {

    MetricsBackend NOOP = new MetricsBackend() {};

    default void recordEvent(String eventType, boolean cancelled) {}

    default void recordObserverFailure() {}

    default void recordRemoteRequest(String method, String route, int status, long durationNanos) {}

    default String scrape() {
        return "";
    }

    @Override
    default void close() {}
}
