package com.monkey.mcbot.api.model;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class BotSpawnRequest {

    private final BotMode mode;
    private final UUID ownerUUID;
    private final Set<UUID> targetUUIDs;
    private final Set<UUID> teamOwnerUUIDs;
    private final BotSettings settings;

    private BotSpawnRequest(Builder builder) {
        this.mode = builder.mode;
        this.ownerUUID = builder.ownerUUID;
        this.targetUUIDs = Set.copyOf(builder.targetUUIDs);
        this.teamOwnerUUIDs = Set.copyOf(builder.teamOwnerUUIDs);
        this.settings = builder.settings;
    }

    public static Builder builder(BotMode mode) {
        return new Builder(mode);
    }

    public BotMode mode() {
        return mode;
    }

    public UUID ownerUUID() {
        return ownerUUID;
    }

    public UUID targetUUID() {
        return targetUUIDs.stream().findFirst().orElse(null);
    }

    public Set<UUID> targetUUIDs() {
        return targetUUIDs;
    }

    public Set<UUID> teamOwnerUUIDs() {
        return teamOwnerUUIDs;
    }

    public BotSettings settings() {
        return settings;
    }

    public static final class Builder {
        private final BotMode mode;
        private UUID ownerUUID;
        private final Set<UUID> targetUUIDs = new LinkedHashSet<>();
        private final Set<UUID> teamOwnerUUIDs = new LinkedHashSet<>();
        private BotSettings settings;

        private Builder(BotMode mode) {
            this.mode = Objects.requireNonNull(mode, "mode");
        }

        public Builder owner(UUID ownerUUID) {
            this.ownerUUID = ownerUUID;
            return this;
        }

        public Builder target(UUID targetUUID) {
            this.targetUUIDs.clear();
            if (targetUUID != null) {
                this.targetUUIDs.add(targetUUID);
            }
            return this;
        }

        public Builder addTarget(UUID targetUUID) {
            if (targetUUID != null) {
                this.targetUUIDs.add(targetUUID);
            }
            return this;
        }

        public Builder targets(Collection<UUID> targets) {
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

        public Builder addTeamOwner(UUID teamOwnerUUID) {
            if (teamOwnerUUID != null) {
                this.teamOwnerUUIDs.add(teamOwnerUUID);
            }
            return this;
        }

        public Builder teamOwners(Collection<UUID> owners) {
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

        public Builder settings(BotSettings settings) {
            this.settings = Objects.requireNonNull(settings, "settings");
            return this;
        }

        public BotSpawnRequest build() {
            if (mode != BotMode.TEAM_ALLY && ownerUUID == null) {
                throw new IllegalArgumentException("ownerUUID is required for non-team bot modes");
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
