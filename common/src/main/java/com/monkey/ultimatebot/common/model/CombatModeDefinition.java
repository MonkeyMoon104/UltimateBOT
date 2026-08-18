package com.monkey.ultimatebot.common.model;

import com.monkey.ultimatebot.common.util.ImmutableCollections;
import com.monkey.ultimatebot.common.util.TextValues;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.jspecify.annotations.Nullable;

public final class CombatModeDefinition {
    private final CombatMode mode;
    private final String displayName;
    private final boolean enabled;
    private final String iconMaterial;
    private final Set<CombatCapability> capabilities;
    private final Map<DifficultyTier, CombatTuning> profiles;
    private final String provider;
    private final List<String> description;
    private final String permission;
    private final int order;
    private final @Nullable BrainKey brain;
    private final Set<PlatformCapability> requiredPlatformCapabilities;

    public CombatModeDefinition(
            CombatMode mode,
            String displayName,
            boolean enabled,
            String iconMaterial,
            Set<CombatCapability> capabilities,
            Map<DifficultyTier, CombatTuning> profiles,
            String provider,
            List<String> description,
            String permission,
            int order,
            @Nullable BrainKey brain) {
        this(
                mode,
                displayName,
                enabled,
                iconMaterial,
                capabilities,
                profiles,
                provider,
                description,
                permission,
                order,
                brain,
                mode.requiredPlatformCapabilities());
    }

    public CombatModeDefinition(
            CombatMode mode,
            String displayName,
            boolean enabled,
            String iconMaterial,
            Set<CombatCapability> capabilities,
            Map<DifficultyTier, CombatTuning> profiles,
            String provider,
            List<String> description,
            String permission,
            int order,
            @Nullable BrainKey brain,
            Set<PlatformCapability> requiredPlatformCapabilities) {
        this.mode = Objects.requireNonNull(mode, "mode");
        this.displayName = requireText(displayName, "displayName");
        this.enabled = enabled;
        this.iconMaterial = requireText(iconMaterial, "iconMaterial");
        this.provider = requireText(provider, "provider");
        this.description = ImmutableCollections.copyOf(Objects.requireNonNull(description, "description"));
        this.permission = Objects.requireNonNull(permission, "permission").trim();
        this.capabilities = ImmutableCollections.copyOf(Objects.requireNonNull(capabilities, "capabilities"));
        this.requiredPlatformCapabilities = ImmutableCollections.copyOf(
                Objects.requireNonNull(requiredPlatformCapabilities, "requiredPlatformCapabilities"));
        EnumMap<DifficultyTier, CombatTuning> profileCopy = new EnumMap<>(Objects.requireNonNull(profiles, "profiles"));
        for (DifficultyTier difficulty : DifficultyTier.values()) {
            Objects.requireNonNull(profileCopy.get(difficulty), "profiles[" + difficulty + "]");
        }
        this.profiles = ImmutableCollections.copyOf(profileCopy);
        this.order = order;
        this.brain = brain;
    }

    public CombatModeDefinition(
            CombatMode mode,
            String displayName,
            boolean enabled,
            String iconMaterial,
            Set<CombatCapability> capabilities,
            Map<DifficultyTier, CombatTuning> profiles) {
        this(
                mode,
                displayName,
                enabled,
                iconMaterial,
                capabilities,
                profiles,
                mode.namespace(),
                ImmutableCollections.emptyList(),
                "",
                0,
                null);
    }

    public CombatMode mode() {
        return mode;
    }

    public String displayName() {
        return displayName;
    }

    public boolean enabled() {
        return enabled;
    }

    public String iconMaterial() {
        return iconMaterial;
    }

    public Set<CombatCapability> capabilities() {
        return capabilities;
    }

    public Map<DifficultyTier, CombatTuning> profiles() {
        return profiles;
    }

    public String provider() {
        return provider;
    }

    public List<String> description() {
        return description;
    }

    public String permission() {
        return permission;
    }

    public int order() {
        return order;
    }

    public @Nullable BrainKey brain() {
        return brain;
    }

    public Set<PlatformCapability> requiredPlatformCapabilities() {
        return requiredPlatformCapabilities;
    }

    public CombatTuning profile(DifficultyTier difficulty) {
        CombatTuning tuning = profiles.get(Objects.requireNonNull(difficulty, "difficulty"));
        if (tuning == null) {
            throw new IllegalStateException("Missing " + mode + " profile for " + difficulty);
        }
        return tuning;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CombatModeDefinition)) {
            return false;
        }
        CombatModeDefinition other = (CombatModeDefinition) obj;
        return enabled == other.enabled
                && order == other.order
                && mode.equals(other.mode)
                && displayName.equals(other.displayName)
                && iconMaterial.equals(other.iconMaterial)
                && capabilities.equals(other.capabilities)
                && profiles.equals(other.profiles)
                && provider.equals(other.provider)
                && description.equals(other.description)
                && permission.equals(other.permission)
                && Objects.equals(brain, other.brain)
                && requiredPlatformCapabilities.equals(other.requiredPlatformCapabilities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                mode,
                displayName,
                enabled,
                iconMaterial,
                capabilities,
                profiles,
                provider,
                description,
                permission,
                order,
                brain,
                requiredPlatformCapabilities);
    }

    @Override
    public String toString() {
        return "CombatModeDefinition[mode="
                + mode
                + ", displayName="
                + displayName
                + ", enabled="
                + enabled
                + ", iconMaterial="
                + iconMaterial
                + ", capabilities="
                + capabilities
                + ", profiles="
                + profiles
                + ", provider="
                + provider
                + ", description="
                + description
                + ", permission="
                + permission
                + ", order="
                + order
                + ", brain="
                + brain
                + ", requiredPlatformCapabilities="
                + requiredPlatformCapabilities
                + ']';
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        if (TextValues.isBlank(value)) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return value;
    }
}
