package com.monkey.ultimatebot.common.model;

import java.util.Objects;
import java.util.Set;
import com.monkey.ultimatebot.common.util.ImmutableCollections;

/** Platform-neutral description of a custom brain exposed through the API and SDK. */
public final class BrainDefinition {
    private final BrainKey key;
    private final String displayName;
    private final String description;
    private final Set<BrainCapability> capabilities;
    private final boolean nativeAccess;
    private final String provider;

    public BrainDefinition(
            BrainKey key,
            String displayName,
            String description,
            Set<BrainCapability> capabilities,
            boolean nativeAccess,
            String provider) {
        this.key = Objects.requireNonNull(key, "key");
        this.displayName = requireText(displayName, "displayName");
        this.description = Objects.requireNonNull(description, "description").trim();
        this.capabilities = ImmutableCollections.copyOf(Objects.requireNonNull(capabilities, "capabilities"));
        this.nativeAccess = nativeAccess;
        this.provider = requireText(provider, "provider");
    }

    public BrainKey key() {
        return key;
    }

    public String displayName() {
        return displayName;
    }

    public String description() {
        return description;
    }

    public Set<BrainCapability> capabilities() {
        return capabilities;
    }

    public boolean nativeAccess() {
        return nativeAccess;
    }

    public String provider() {
        return provider;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BrainDefinition)) {
            return false;
        }
        BrainDefinition other = (BrainDefinition) obj;
        return nativeAccess == other.nativeAccess
                && key.equals(other.key)
                && displayName.equals(other.displayName)
                && description.equals(other.description)
                && capabilities.equals(other.capabilities)
                && provider.equals(other.provider);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, displayName, description, capabilities, nativeAccess, provider);
    }

    @Override
    public String toString() {
        return "BrainDefinition[key="
                + key
                + ", displayName="
                + displayName
                + ", description="
                + description
                + ", capabilities="
                + capabilities
                + ", nativeAccess="
                + nativeAccess
                + ", provider="
                + provider
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
