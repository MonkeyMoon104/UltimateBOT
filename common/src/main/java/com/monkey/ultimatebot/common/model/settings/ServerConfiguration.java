package com.monkey.ultimatebot.common.model.settings;

import java.util.Objects;

/** Read-only view of server-wide UltimateBot configuration exposed to integrations. */
public final class ServerConfiguration {

    private static final ServerConfiguration DEFAULTS =
            new ServerConfiguration(VanillaStatisticsSettings.defaults(), 1);

    private final VanillaStatisticsSettings vanillaStatistics;
    private final int configVersion;

    public ServerConfiguration(VanillaStatisticsSettings vanillaStatistics, int configVersion) {
        this.vanillaStatistics = Objects.requireNonNull(vanillaStatistics, "vanillaStatistics");
        if (configVersion < 0) {
            throw new IllegalArgumentException("configVersion cannot be negative");
        }
        this.configVersion = configVersion;
    }

    public static ServerConfiguration defaults() {
        return DEFAULTS;
    }

    public VanillaStatisticsSettings vanillaStatistics() {
        return vanillaStatistics;
    }

    public int configVersion() {
        return configVersion;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ServerConfiguration)) {
            return false;
        }
        ServerConfiguration other = (ServerConfiguration) obj;
        return configVersion == other.configVersion && vanillaStatistics.equals(other.vanillaStatistics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vanillaStatistics, configVersion);
    }

    @Override
    public String toString() {
        return "ServerConfiguration[vanillaStatistics="
                + vanillaStatistics
                + ", configVersion="
                + configVersion
                + ']';
    }
}
