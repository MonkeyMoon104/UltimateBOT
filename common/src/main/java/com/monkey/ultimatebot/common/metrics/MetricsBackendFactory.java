package com.monkey.ultimatebot.common.metrics;

@FunctionalInterface
public interface MetricsBackendFactory {

    MetricsBackend create(MetricsBackendContext context);
}
