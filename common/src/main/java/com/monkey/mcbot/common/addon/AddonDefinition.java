package com.monkey.mcbot.common.addon;

import java.util.Objects;

/** Immutable coordinates used to download and load one optional addon. */
public record AddonDefinition(
        String id, String displayName, String fileName, String descriptorResource, String overrideUrlProperty) {
    public AddonDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(displayName, "displayName");
        Objects.requireNonNull(fileName, "fileName");
        Objects.requireNonNull(descriptorResource, "descriptorResource");
        Objects.requireNonNull(overrideUrlProperty, "overrideUrlProperty");
    }
}
