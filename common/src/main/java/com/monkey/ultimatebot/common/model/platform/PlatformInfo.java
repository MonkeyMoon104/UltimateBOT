package com.monkey.ultimatebot.common.model.platform;

import com.monkey.ultimatebot.common.util.ImmutableCollections;
import java.util.Objects;
import java.util.Set;

/** Runtime Minecraft version and feature flags for the loaded NMS bridge. */
public final class PlatformInfo {
    private final String minecraftVersion;
    private final String supportedVersions;
    private final Set<PlatformCapability> capabilities;
    private final boolean botRuntimeSupported;

    public PlatformInfo(
            String minecraftVersion,
            String supportedVersions,
            Set<PlatformCapability> capabilities,
            boolean botRuntimeSupported) {
        this.minecraftVersion = requireText(minecraftVersion, "minecraftVersion");
        this.supportedVersions = requireText(supportedVersions, "supportedVersions");
        this.capabilities = ImmutableCollections.copyOf(Objects.requireNonNull(capabilities, "capabilities"));
        this.botRuntimeSupported = botRuntimeSupported;
    }

    public String minecraftVersion() {
        return minecraftVersion;
    }

    public String supportedVersions() {
        return supportedVersions;
    }

    public Set<PlatformCapability> capabilities() {
        return capabilities;
    }

    public boolean botRuntimeSupported() {
        return botRuntimeSupported;
    }

    public boolean supports(PlatformCapability capability) {
        return capabilities.contains(Objects.requireNonNull(capability, "capability"));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PlatformInfo)) {
            return false;
        }
        PlatformInfo other = (PlatformInfo) obj;
        return botRuntimeSupported == other.botRuntimeSupported
                && minecraftVersion.equals(other.minecraftVersion)
                && supportedVersions.equals(other.supportedVersions)
                && capabilities.equals(other.capabilities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(minecraftVersion, supportedVersions, capabilities, botRuntimeSupported);
    }

    @Override
    public String toString() {
        return "PlatformInfo[minecraftVersion="
                + minecraftVersion
                + ", supportedVersions="
                + supportedVersions
                + ", capabilities="
                + capabilities
                + ", botRuntimeSupported="
                + botRuntimeSupported
                + ']';
    }

    private static String requireText(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return checked;
    }
}
