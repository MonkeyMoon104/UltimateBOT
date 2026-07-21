package com.monkey.mcbot.sdk.model;

import java.util.List;
import java.util.UUID;

public record EventBotSpawnRequest(
        UUID ownerUUID,
        List<UUID> targetUUIDs,
        String botNameTemplate,
        String botSkin,
        boolean follow,
        boolean combat,
        String armor,
        String minArmor,
        String maxArmor,
        int totemCount,
        int minTotemCount,
        int maxTotemCount,
        String rank,
        String minRank,
        String maxRank,
        BotLocationRequest spawnLocation,
        boolean autoTarget,
        double autoTargetRange,
        boolean respectWorldGuardPvp,
        boolean stayAfterOwnerDeath,
        boolean idleWander,
        double idleWanderRadius,
        double idleReturnDistance,
        long idleReturnDelayMs,
        boolean crystalPvp,
        boolean explosions,
        boolean enderPearls,
        boolean killMessageEnabled,
        String killMessage
) {
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private UUID ownerUUID;
        private List<UUID> targetUUIDs = List.of();
        private String botNameTemplate = "MinecraftBot";
        private String botSkin = "RANDOM";
        private boolean follow = true;
        private boolean combat = true;
        private String armor = "NETHERITE";
        private String minArmor = "LEATHER";
        private String maxArmor = "NETHERITE";
        private int totemCount = -1;
        private int minTotemCount = -1;
        private int maxTotemCount = 74;
        private String rank = "EASY";
        private String minRank = "EASY";
        private String maxRank = "GOD";
        private BotLocationRequest spawnLocation;
        private boolean autoTarget = true;
        private double autoTargetRange = 16.0D;
        private boolean respectWorldGuardPvp = false;
        private boolean stayAfterOwnerDeath = false;
        private boolean idleWander = false;
        private double idleWanderRadius = 10.0D;
        private double idleReturnDistance = 24.0D;
        private long idleReturnDelayMs = 8000L;
        private boolean crystalPvp = true;
        private boolean explosions = true;
        private boolean enderPearls = true;
        private boolean killMessageEnabled = true;
        private String killMessage;

        private Builder() {
        }

        public Builder ownerUUID(UUID ownerUUID) { this.ownerUUID = ownerUUID; return this; }
        public Builder targetUUIDs(List<UUID> targetUUIDs) { this.targetUUIDs = targetUUIDs == null ? List.of() : List.copyOf(targetUUIDs); return this; }
        public Builder botNameTemplate(String botNameTemplate) { this.botNameTemplate = botNameTemplate; return this; }
        public Builder botSkin(String botSkin) { this.botSkin = botSkin; return this; }
        public Builder follow(boolean follow) { this.follow = follow; return this; }
        public Builder combat(boolean combat) { this.combat = combat; return this; }
        public Builder armor(String armor) { this.armor = armor; return this; }
        public Builder armorRange(String minArmor, String maxArmor) { this.minArmor = minArmor; this.maxArmor = maxArmor; return this; }
        public Builder totemCount(int totemCount) { this.totemCount = totemCount; return this; }
        public Builder totemRange(int minTotemCount, int maxTotemCount) { this.minTotemCount = minTotemCount; this.maxTotemCount = maxTotemCount; return this; }
        public Builder rank(String rank) { this.rank = rank; return this; }
        public Builder rankRange(String minRank, String maxRank) { this.minRank = minRank; this.maxRank = maxRank; return this; }
        public Builder spawnLocation(BotLocationRequest spawnLocation) { this.spawnLocation = spawnLocation; return this; }
        public Builder autoTarget(boolean autoTarget) { this.autoTarget = autoTarget; return this; }
        public Builder autoTargetRange(double autoTargetRange) { this.autoTargetRange = autoTargetRange; return this; }
        public Builder respectWorldGuardPvp(boolean respectWorldGuardPvp) { this.respectWorldGuardPvp = respectWorldGuardPvp; return this; }
        public Builder stayAfterOwnerDeath(boolean stayAfterOwnerDeath) { this.stayAfterOwnerDeath = stayAfterOwnerDeath; return this; }
        public Builder idleWander(boolean idleWander) { this.idleWander = idleWander; return this; }
        public Builder idleWanderRadius(double idleWanderRadius) { this.idleWanderRadius = idleWanderRadius; return this; }
        public Builder idleReturnDistance(double idleReturnDistance) { this.idleReturnDistance = idleReturnDistance; return this; }
        public Builder idleReturnDelayMs(long idleReturnDelayMs) { this.idleReturnDelayMs = idleReturnDelayMs; return this; }
        public Builder crystalPvp(boolean crystalPvp) { this.crystalPvp = crystalPvp; return this; }
        public Builder explosions(boolean explosions) { this.explosions = explosions; return this; }
        public Builder enderPearls(boolean enderPearls) { this.enderPearls = enderPearls; return this; }
        public Builder killMessage(String killMessage) { this.killMessageEnabled = true; this.killMessage = killMessage; return this; }
        public Builder disableKillMessage() { this.killMessageEnabled = false; this.killMessage = null; return this; }

        public EventBotSpawnRequest build() {
            return new EventBotSpawnRequest(
                    ownerUUID,
                    targetUUIDs,
                    botNameTemplate,
                    botSkin,
                    follow,
                    combat,
                    armor,
                    minArmor,
                    maxArmor,
                    totemCount,
                    minTotemCount,
                    maxTotemCount,
                    rank,
                    minRank,
                    maxRank,
                    spawnLocation,
                    autoTarget,
                    autoTargetRange,
                    respectWorldGuardPvp,
                    stayAfterOwnerDeath,
                    idleWander,
                    idleWanderRadius,
                    idleReturnDistance,
                    idleReturnDelayMs,
                    crystalPvp,
                    explosions,
                    enderPearls,
                    killMessageEnabled,
                    killMessage
            );
        }
    }
}
