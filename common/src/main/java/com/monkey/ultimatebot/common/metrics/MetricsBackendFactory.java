package com.monkey.ultimatebot.common.metrics;

/** Creates an optional metrics backend from Java-only runtime suppliers. */
@FunctionalInterface
public interface MetricsBackendFactory {
    /** Creates a backend for the supplied runtime context. */
    MetricsBackend create(MetricsBackendContext context);
}
