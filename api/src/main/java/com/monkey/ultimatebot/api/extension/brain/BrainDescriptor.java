package com.monkey.ultimatebot.api.extension.brain;


import java.util.Collections;
import com.monkey.ultimatebot.common.model.BrainCapability;
import com.monkey.ultimatebot.common.model.BrainKey;
import java.util.Objects;
import java.util.Set;

/** Immutable identity and ownership declaration for a custom bot brain. */
public final class BrainDescriptor {
    private final BrainKey key;
    private final String displayName;
    private final String description;
    private final Set<BrainCapability> capabilities;
    private final boolean nativeAccess;

    public BrainDescriptor(BrainKey key, String displayName, String description, Set<BrainCapability> capabilities, boolean nativeAccess) {


        Objects.requireNonNull(key, "key");
        displayName = requireText(displayName, "displayName");
        description = Objects.requireNonNull(description, "description").trim();
        capabilities = com.monkey.ultimatebot.common.util.ImmutableCollections.copyOf(Objects.requireNonNull(capabilities, "capabilities"));
        if (capabilities.isEmpty()) {
            throw new IllegalArgumentException("capabilities cannot be empty");
        }
        this.key = key;
        this.displayName = displayName;
        this.description = description;
        this.capabilities = capabilities;
        this.nativeAccess = nativeAccess;
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

    public boolean fullControl() {
        return capabilities.contains(BrainCapability.FULL_CONTROL);
    }

    private static String requireText(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return checked;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BrainDescriptor)) {
            return false;
        }
        BrainDescriptor other = (BrainDescriptor) obj;
        return java.util.Objects.equals(key, other.key) && java.util.Objects.equals(displayName, other.displayName) && java.util.Objects.equals(description, other.description) && java.util.Objects.equals(capabilities, other.capabilities) && nativeAccess == other.nativeAccess;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(key, displayName, description, capabilities, nativeAccess);
    }

    @Override
    public String toString() {
        return "BrainDescriptor[key=" + key + ", displayName=" + displayName + ", description=" + description + ", capabilities=" + capabilities + ", nativeAccess=" + nativeAccess + "]";
    }
}
