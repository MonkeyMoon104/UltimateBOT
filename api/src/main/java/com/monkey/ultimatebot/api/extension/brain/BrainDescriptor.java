package com.monkey.ultimatebot.api.extension.brain;

import com.monkey.ultimatebot.common.model.BrainCapability;
import com.monkey.ultimatebot.common.model.BrainKey;
import java.util.Objects;
import java.util.Set;

/** Immutable identity and ownership declaration for a custom bot brain. */
public record BrainDescriptor(
        BrainKey key, String displayName, String description, Set<BrainCapability> capabilities, boolean nativeAccess) {
    public BrainDescriptor {
        Objects.requireNonNull(key, "key");
        displayName = requireText(displayName, "displayName");
        description = Objects.requireNonNull(description, "description").trim();
        capabilities = Set.copyOf(Objects.requireNonNull(capabilities, "capabilities"));
        if (capabilities.isEmpty()) {
            throw new IllegalArgumentException("capabilities cannot be empty");
        }
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
}
