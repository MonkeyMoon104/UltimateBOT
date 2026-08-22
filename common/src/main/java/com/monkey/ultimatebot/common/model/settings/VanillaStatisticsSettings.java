package com.monkey.ultimatebot.common.model.settings;

import java.util.Objects;

/** Controls whether vanilla Minecraft kill/death statistics change during bot fights. */
public final class VanillaStatisticsSettings {

    private static final VanillaStatisticsSettings DEFAULTS = new VanillaStatisticsSettings(true, true);

    private final boolean trackKills;
    private final boolean trackDeaths;

    public VanillaStatisticsSettings(boolean trackKills, boolean trackDeaths) {
        this.trackKills = trackKills;
        this.trackDeaths = trackDeaths;
    }

    public static VanillaStatisticsSettings defaults() {
        return DEFAULTS;
    }

    /** When {@code true}, killing an UltimateBot awards {@code PLAYER_KILLS} to the killer. */
    public boolean trackKills() {
        return trackKills;
    }

    /** When {@code true}, dying to an UltimateBot awards {@code DEATHS} to the victim. */
    public boolean trackDeaths() {
        return trackDeaths;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof VanillaStatisticsSettings)) {
            return false;
        }
        VanillaStatisticsSettings other = (VanillaStatisticsSettings) obj;
        return trackKills == other.trackKills && trackDeaths == other.trackDeaths;
    }

    @Override
    public int hashCode() {
        return Objects.hash(trackKills, trackDeaths);
    }

    @Override
    public String toString() {
        return "VanillaStatisticsSettings[trackKills=" + trackKills + ", trackDeaths=" + trackDeaths + ']';
    }
}
