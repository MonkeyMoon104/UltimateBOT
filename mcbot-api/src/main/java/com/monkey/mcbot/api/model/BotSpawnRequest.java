package com.monkey.mcbot.api.model;

import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * Immutable spawn request used by {@code IBotManager.spawn(...)}.
 *
 * <p>The request includes mode, ownership, targets and settings.
 * Validation is enforced in the builder {@link Builder#build()}.</p>
 */
public final class BotSpawnRequest {

    private final BotMode mode;
    private final @Nullable UUID ownerUUID;
    private final Set<UUID> targetUUIDs;
    private final Set<UUID> teamOwnerUUIDs;
    private final BotSettings settings;

    private BotSpawnRequest(Builder builder) {
        this.mode = builder.mode;
        this.ownerUUID = builder.ownerUUID;
        this.targetUUIDs = Set.copyOf(builder.targetUUIDs);
        this.teamOwnerUUIDs = Set.copyOf(builder.teamOwnerUUIDs);
        this.settings = Objects.requireNonNull(builder.settings, "settings");
    }

    /**
     * Creates a new builder for the given mode.
     *
     * @param mode spawn mode
     * @return builder instance
     */
    public static Builder builder(BotMode mode) {
        return new Builder(mode);
    }

    /**
     * Returns the selected spawn mode.
     *
     * @return mode
     */
    public BotMode mode() {
        return mode;
    }

    /**
     * Returns the primary owner UUID.
     *
     * @return primary owner UUID, can be null for EVENT and TEAM_ALLY until runtime resolution
     */
    public @Nullable UUID ownerUUID() {
        return ownerUUID;
    }

    /**
     * Returns the first target UUID, if available.
     *
     * @return first target UUID or {@code null}
     */
    public @Nullable UUID targetUUID() {
        return targetUUIDs.stream().findFirst().orElse(null);
    }

    /**
     * Returns all target UUIDs.
     *
     * @return immutable target set
     */
    public Set<UUID> targetUUIDs() {
        return targetUUIDs;
    }

    /**
     * Returns all TEAM_ALLY owner UUIDs.
     *
     * @return immutable team-owner set
     */
    public Set<UUID> teamOwnerUUIDs() {
        return teamOwnerUUIDs;
    }

    /**
     * Returns spawn settings.
     *
     * @return bot settings
     */
    public BotSettings settings() {
        return settings;
    }

    /**
     * Builder for {@link BotSpawnRequest}.
     */
    public static final class Builder {
        private final BotMode mode;
        private @Nullable UUID ownerUUID;
        private final Set<UUID> targetUUIDs = new LinkedHashSet<>();
        private final Set<UUID> teamOwnerUUIDs = new LinkedHashSet<>();
        private @Nullable BotSettings settings;

        /**
         * Creates a builder for the provided mode.
         *
         * @param mode spawn mode
         */
        private Builder(BotMode mode) {
            this.mode = Objects.requireNonNull(mode, "mode");
        }

        /**
         * Sets the primary owner UUID.
         *
         * @param ownerUUID primary owner
         * @return current builder
         */
        public Builder owner(@Nullable UUID ownerUUID) {
            this.ownerUUID = ownerUUID;
            return this;
        }

        /**
         * Replaces current targets with a single target UUID.
         *
         * @param targetUUID target UUID (null clears targets)
         * @return current builder
         */
        public Builder target(@Nullable UUID targetUUID) {
            this.targetUUIDs.clear();
            if (targetUUID != null) {
                this.targetUUIDs.add(targetUUID);
            }
            return this;
        }

        /**
         * Adds a single target UUID to the current target set.
         *
         * @param targetUUID target UUID
         * @return current builder
         */
        public Builder addTarget(@Nullable UUID targetUUID) {
            if (targetUUID != null) {
                this.targetUUIDs.add(targetUUID);
            }
            return this;
        }

        /**
         * Replaces all current targets using the provided collection.
         *
         * <p>Null entries are ignored.</p>
         *
         * @param targets target collection
         * @return current builder
         */
        public Builder targets(@Nullable Collection<@Nullable UUID> targets) {
            this.targetUUIDs.clear();
            if (targets != null) {
                for (UUID target : targets) {
                    if (target != null) {
                        this.targetUUIDs.add(target);
                    }
                }
            }
            return this;
        }

        /**
         * Adds a TEAM_ALLY owner UUID.
         *
         * @param teamOwnerUUID owner UUID
         * @return current builder
         */
        public Builder addTeamOwner(@Nullable UUID teamOwnerUUID) {
            if (teamOwnerUUID != null) {
                this.teamOwnerUUIDs.add(teamOwnerUUID);
            }
            return this;
        }

        /**
         * Replaces TEAM_ALLY owners using the provided collection.
         *
         * <p>Null entries are ignored.</p>
         *
         * @param owners owner collection
         * @return current builder
         */
        public Builder teamOwners(@Nullable Collection<@Nullable UUID> owners) {
            this.teamOwnerUUIDs.clear();
            if (owners != null) {
                for (UUID owner : owners) {
                    if (owner != null) {
                        this.teamOwnerUUIDs.add(owner);
                    }
                }
            }
            return this;
        }

        /**
         * Sets required bot settings.
         *
         * @param settings spawn settings
         * @return current builder
         */
        public Builder settings(BotSettings settings) {
            this.settings = Objects.requireNonNull(settings, "settings");
            return this;
        }

        /**
         * Builds an immutable spawn request after validating mode constraints.
         *
         * <p>Validation rules include:
         * owner requirement for SINGLE and ALLY modes, TEAM_ALLY owner presence, non-null settings,
         * and SINGLE mode target auto-forcing to owner.</p>
         *
         * @return immutable spawn request
         * @throws IllegalArgumentException when required data is missing or invalid for the mode
         */
        public BotSpawnRequest build() {
            if ((mode == BotMode.SINGLE || mode == BotMode.ALLY) && ownerUUID == null) {
                throw new IllegalArgumentException("ownerUUID is required for single and ally bot modes");
            }

            if (mode == BotMode.TEAM_ALLY && teamOwnerUUIDs.isEmpty() && ownerUUID == null) {
                throw new IllegalArgumentException("TEAM_ALLY requires at least one owner");
            }

            if (settings == null) {
                throw new IllegalArgumentException("settings is required");
            }

            if (mode == BotMode.SINGLE) {
                targetUUIDs.clear();
                targetUUIDs.add(ownerUUID);
            }

            return new BotSpawnRequest(this);
        }
    }
}
