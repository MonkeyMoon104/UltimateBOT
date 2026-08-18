package com.monkey.ultimatebot.metrics.addon;

import com.monkey.ultimatebot.common.metrics.MetricsBackend;
import com.monkey.ultimatebot.common.metrics.MetricsBackendContext;
import com.monkey.ultimatebot.common.metrics.MetricsBackendFactory;

public final class MicrometerMetricsBackendFactory implements MetricsBackendFactory {
    @Override
    public MetricsBackend create(MetricsBackendContext context) {
        return new MicrometerMetricsBackend(context);
    }
}
