package com.monkey.ultimatebot.common.addon;

import java.util.Objects;

public final class AddonDefinition {
    private final String id;
    private final String displayName;
    private final String fileName;
    private final String descriptorResource;
    private final String overrideUrlProperty;

    public AddonDefinition(
            String id, String displayName, String fileName, String descriptorResource, String overrideUrlProperty) {
        this.id = Objects.requireNonNull(id, "id");
        this.displayName = Objects.requireNonNull(displayName, "displayName");
        this.fileName = Objects.requireNonNull(fileName, "fileName");
        this.descriptorResource = Objects.requireNonNull(descriptorResource, "descriptorResource");
        this.overrideUrlProperty = Objects.requireNonNull(overrideUrlProperty, "overrideUrlProperty");
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public String fileName() {
        return fileName;
    }

    public String descriptorResource() {
        return descriptorResource;
    }

    public String overrideUrlProperty() {
        return overrideUrlProperty;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AddonDefinition)) {
            return false;
        }
        AddonDefinition other = (AddonDefinition) obj;
        return id.equals(other.id)
                && displayName.equals(other.displayName)
                && fileName.equals(other.fileName)
                && descriptorResource.equals(other.descriptorResource)
                && overrideUrlProperty.equals(other.overrideUrlProperty);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, displayName, fileName, descriptorResource, overrideUrlProperty);
    }

    @Override
    public String toString() {
        return "AddonDefinition[id="
                + id
                + ", displayName="
                + displayName
                + ", fileName="
                + fileName
                + ", descriptorResource="
                + descriptorResource
                + ", overrideUrlProperty="
                + overrideUrlProperty
                + ']';
    }
}
