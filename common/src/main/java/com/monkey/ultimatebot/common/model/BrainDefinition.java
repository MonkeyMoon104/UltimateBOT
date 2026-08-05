package com.monkey.ultimatebot.common.model;

import java.util.Objects;
import java.util.Set;

/** Platform-neutral description of a custom brain exposed through the API and SDK. */
public record BrainDefinition(
        BrainKey key,
        String displayName,
        String description,
        Set<BrainCapability> capabilities,
        boolean nativeAccess,
        String provider) {
    public BrainDefinition {
        Objects.requireNonNull(key, "key");
        displayName = requireText(displayName, "displayName");
        description = Objects.requireNonNull(description, "description").trim();
        capabilities = Set.copyOf(Objects.requireNonNull(capabilities, "capabilities"));
        provider = requireText(provider, "provider");
    }

    private static String requireText(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return checked;
    }
}
