package com.monkey.ultimatebot.api.extension.combat;

import com.monkey.ultimatebot.common.model.BrainKey;
import com.monkey.ultimatebot.common.model.CombatCapability;
import com.monkey.ultimatebot.common.model.CombatMode;
import com.monkey.ultimatebot.common.model.CombatTuning;
import com.monkey.ultimatebot.common.model.DifficultyTier;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.bukkit.Material;
import org.jspecify.annotations.Nullable;

/** Complete public metadata and defaults for one dynamically registered combat mode. */
public record CombatModeDescriptor(
        CombatMode mode,
        String displayName,
        List<String> description,
        Material icon,
        String permission,
        int order,
        Set<CombatCapability> capabilities,
        Map<DifficultyTier, CombatTuning> profiles,
        ModeKit kit,
        @Nullable BrainKey brain) {
    public CombatModeDescriptor {
        Objects.requireNonNull(mode, "mode");
        displayName = requireText(displayName, "displayName");
        description = List.copyOf(Objects.requireNonNull(description, "description"));
        Objects.requireNonNull(icon, "icon");
        permission = Objects.requireNonNull(permission, "permission").trim();
        capabilities = Set.copyOf(Objects.requireNonNull(capabilities, "capabilities"));
        EnumMap<DifficultyTier, CombatTuning> profileCopy = new EnumMap<>(Objects.requireNonNull(profiles, "profiles"));
        for (DifficultyTier difficulty : DifficultyTier.values()) {
            Objects.requireNonNull(profileCopy.get(difficulty), "profiles[" + difficulty + "]");
        }
        profiles = Map.copyOf(profileCopy);
        Objects.requireNonNull(kit, "kit");
    }

    public Optional<BrainKey> brainKey() {
        return Optional.ofNullable(brain);
    }

    /** Starts a descriptor builder with validated identity and presentation defaults. */
    public static Builder builder(CombatMode mode, String displayName, Material icon) {
        return new Builder(mode, displayName, icon);
    }

    public CombatTuning profile(DifficultyTier difficulty) {
        return Objects.requireNonNull(profiles.get(Objects.requireNonNull(difficulty, "difficulty")), "profile");
    }

    private static String requireText(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return checked;
    }

    /** Fluent builder for addon-provided mode metadata, profiles and kit. */
    public static final class Builder {
        private final CombatMode mode;
        private final String displayName;
        private final Material icon;
        private final EnumMap<DifficultyTier, CombatTuning> profiles = new EnumMap<>(DifficultyTier.class);
        private List<String> description = List.of();
        private String permission = "";
        private int order;
        private Set<CombatCapability> capabilities = Set.of();
        private ModeKit kit = ModeKit.empty();
        private @Nullable BrainKey brain;

        private Builder(CombatMode mode, String displayName, Material icon) {
            this.mode = Objects.requireNonNull(mode, "mode");
            this.displayName = requireText(displayName, "displayName");
            this.icon = Objects.requireNonNull(icon, "icon");
            for (DifficultyTier difficulty : DifficultyTier.values()) {
                profiles.put(difficulty, CombatTuning.builder().build());
            }
        }

        public Builder description(List<String> description) {
            this.description = List.copyOf(Objects.requireNonNull(description, "description"));
            return this;
        }

        public Builder permission(String permission) {
            this.permission = Objects.requireNonNull(permission, "permission").trim();
            return this;
        }

        public Builder order(int order) {
            this.order = order;
            return this;
        }

        public Builder capabilities(Set<CombatCapability> capabilities) {
            this.capabilities = Set.copyOf(Objects.requireNonNull(capabilities, "capabilities"));
            return this;
        }

        public Builder profile(DifficultyTier difficulty, CombatTuning tuning) {
            profiles.put(Objects.requireNonNull(difficulty, "difficulty"), Objects.requireNonNull(tuning, "tuning"));
            return this;
        }

        public Builder profiles(Map<DifficultyTier, CombatTuning> profiles) {
            this.profiles.putAll(Objects.requireNonNull(profiles, "profiles"));
            return this;
        }

        public Builder kit(ModeKit kit) {
            this.kit = Objects.requireNonNull(kit, "kit");
            return this;
        }

        public Builder brain(@Nullable BrainKey brain) {
            this.brain = brain;
            return this;
        }

        public CombatModeDescriptor build() {
            return new CombatModeDescriptor(
                    mode, displayName, description, icon, permission, order, capabilities, profiles, kit, brain);
        }
    }
}
