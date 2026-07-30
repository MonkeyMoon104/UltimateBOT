package com.monkey.mcbot.metrics.addon;

import com.monkey.mcbot.common.metrics.MetricsBackend;
import com.monkey.mcbot.common.metrics.MetricsBackendContext;
import com.monkey.mcbot.common.metrics.MetricsBackendFactory;

/** Creates the isolated Micrometer runtime loaded by MinecraftBot. */
public final class MicrometerMetricsBackendFactory implements MetricsBackendFactory {
    @Override
    public MetricsBackend create(MetricsBackendContext context) {
        return new MicrometerMetricsBackend(context);
    }
}
