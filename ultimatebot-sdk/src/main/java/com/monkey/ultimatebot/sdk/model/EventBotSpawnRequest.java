package com.monkey.ultimatebot.sdk.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Request body used to spawn one remote event bot.
 *
 * @param ownerUUID optional owner UUID for owner-bound use cases
 * @param botUUID optional explicit bot UUID; {@code null} generates one automatically
 * @param targetUUIDs explicit target players for independent/event bots
 * @param botNameTemplate bot display name or name template
 * @param botSkin skin mode or player/texture reference
 * @param follow whether follow logic is active
 * @param combat whether combat logic is active
 * @param armor selected armor tier
 * @param minArmor minimum random armor tier
 * @param maxArmor maximum random armor tier
 * @param totemCount selected totem count, or negative for random range
 * @param minTotemCount minimum random totem count
 * @param maxTotemCount maximum random totem count
 * @param rank selected rank profile
 * @param minRank minimum random rank profile
 * @param maxRank maximum random rank profile
 * @param spawnLocation optional explicit spawn position
 * @param autoTarget whether automatic target acquisition is enabled
 * @param autoTargetRange target acquisition range
 * @param attackBots whether this bot may target other managed bots
 * @param targetMode categories of living entities this bot may attack
 * @param respectWorldGuardPvp whether PvP-disabled WorldGuard regions are avoided
 * @param stayAfterOwnerDeath whether the bot remains after owner death
 * @param idleWander whether idle wandering is enabled
 * @param idleWanderRadius idle wandering radius
 * @param idleReturnDistance distance that triggers return to spawn
 * @param idleReturnDelayMs delay before returning to spawn
 * @param crystalPvp whether crystal PvP logic is enabled
 * @param explosions whether explosive combat is enabled
 * @param explosionBlockDamage whether bot explosions may destroy terrain
 * @param enderPearls whether ender pearl logic is enabled
 * @param healing whether healing logic is enabled
 * @param killMessageEnabled whether the built-in kill message is enabled
 * @param killMessage custom kill message, or null for default
 * @param equipmentSlots equipment-slot overrides kept active while the bot is running
 */
public record EventBotSpawnRequest(
        @Nullable UUID ownerUUID,
        @Nullable UUID botUUID,
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
        @Nullable BotLocationRequest spawnLocation,
        boolean autoTarget,
        double autoTargetRange,
        boolean attackBots,
        SdkBotTargetMode targetMode,
        boolean respectWorldGuardPvp,
        boolean stayAfterOwnerDeath,
        boolean idleWander,
        double idleWanderRadius,
        double idleReturnDistance,
        long idleReturnDelayMs,
        boolean crystalPvp,
        boolean explosions,
        boolean explosionBlockDamage,
        boolean enderPearls,
        boolean healing,
        boolean killMessageEnabled,
        @Nullable String killMessage,
        Map<SdkBotEquipmentSlot, BotEquipmentSlotRequest> equipmentSlots) {
    public EventBotSpawnRequest {
        targetUUIDs = targetUUIDs == null ? List.of() : List.copyOf(targetUUIDs);
        equipmentSlots = equipmentSlots == null ? Map.of() : Map.copyOf(equipmentSlots);
        Objects.requireNonNull(botNameTemplate, "botNameTemplate");
        Objects.requireNonNull(botSkin, "botSkin");
        Objects.requireNonNull(armor, "armor");
        Objects.requireNonNull(minArmor, "minArmor");
        Objects.requireNonNull(maxArmor, "maxArmor");
        Objects.requireNonNull(rank, "rank");
        Objects.requireNonNull(minRank, "minRank");
        Objects.requireNonNull(maxRank, "maxRank");
        Objects.requireNonNull(targetMode, "targetMode");
    }

    /**
     * Creates a spawn request using the pre-1.5.0 component set.
     *
     * <p>This overload preserves binary compatibility for integrations compiled against older SDK
     * releases. The bot UUID is generated automatically and all equipment slots use their normal
     * bot behavior.
     */
    public EventBotSpawnRequest(
            @Nullable UUID ownerUUID,
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
            @Nullable BotLocationRequest spawnLocation,
            boolean autoTarget,
            double autoTargetRange,
            boolean attackBots,
            SdkBotTargetMode targetMode,
            boolean respectWorldGuardPvp,
            boolean stayAfterOwnerDeath,
            boolean idleWander,
            double idleWanderRadius,
            double idleReturnDistance,
            long idleReturnDelayMs,
            boolean crystalPvp,
            boolean explosions,
            boolean explosionBlockDamage,
            boolean enderPearls,
            boolean healing,
            boolean killMessageEnabled,
            @Nullable String killMessage) {
        this(
                ownerUUID,
                null,
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
                attackBots,
                targetMode,
                respectWorldGuardPvp,
                stayAfterOwnerDeath,
                idleWander,
                idleWanderRadius,
                idleReturnDistance,
                idleReturnDelayMs,
                crystalPvp,
                explosions,
                explosionBlockDamage,
                enderPearls,
                healing,
                killMessageEnabled,
                killMessage,
                Map.of());
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a builder for an independent event bot without owner UUID.
     *
     * @return request builder
     */
    public static Builder independent() {
        return builder().ownerUUID(null);
    }

    /**
     * Creates a builder for a bot bound to one owner UUID.
     *
     * @param ownerUUID owner UUID
     * @return request builder
     */
    public static Builder ownedBy(UUID ownerUUID) {
        return builder().ownerUUID(ownerUUID);
    }

    public static final class Builder {
        private @Nullable UUID ownerUUID;
        private @Nullable UUID botUUID;
        private List<UUID> targetUUIDs = List.of();
        private String botNameTemplate = "UltimateBot";
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
        private @Nullable BotLocationRequest spawnLocation;
        private boolean autoTarget = true;
        private double autoTargetRange = 16.0D;
        private boolean attackBots = false;
        private SdkBotTargetMode targetMode = SdkBotTargetMode.PLAYERS;
        private boolean respectWorldGuardPvp = false;
        private boolean stayAfterOwnerDeath = false;
        private boolean idleWander = false;
        private double idleWanderRadius = 10.0D;
        private double idleReturnDistance = 24.0D;
        private long idleReturnDelayMs = 8000L;
        private boolean crystalPvp = true;
        private boolean explosions = true;
        private boolean explosionBlockDamage = false;
        private boolean enderPearls = true;
        private boolean healing = true;
        private boolean killMessageEnabled = true;
        private @Nullable String killMessage;
        private Map<SdkBotEquipmentSlot, BotEquipmentSlotRequest> equipmentSlots = Map.of();

        private Builder() {}

        public Builder ownerUUID(@Nullable UUID ownerUUID) {
            this.ownerUUID = ownerUUID;
            return this;
        }

        /** Sets a specific bot UUID, or {@code null} to generate one automatically. */
        public Builder botUUID(@Nullable UUID botUUID) {
            this.botUUID = botUUID;
            return this;
        }

        public Builder targetUUIDs(@Nullable List<UUID> targetUUIDs) {
            this.targetUUIDs = targetUUIDs == null ? List.of() : List.copyOf(targetUUIDs);
            return this;
        }

        public Builder botNameTemplate(String botNameTemplate) {
            this.botNameTemplate = Objects.requireNonNull(botNameTemplate, "botNameTemplate");
            return this;
        }

        public Builder botSkin(String botSkin) {
            this.botSkin = Objects.requireNonNull(botSkin, "botSkin");
            return this;
        }

        public Builder follow(boolean follow) {
            this.follow = follow;
            return this;
        }

        public Builder combat(boolean combat) {
            this.combat = combat;
            return this;
        }

        public Builder armor(String armor) {
            this.armor = Objects.requireNonNull(armor, "armor");
            return this;
        }

        public Builder armor(SdkBotArmor armor) {
            this.armor = Objects.requireNonNull(armor, "armor").apiValue();
            return this;
        }

        public Builder armorRange(String minArmor, String maxArmor) {
            this.minArmor = Objects.requireNonNull(minArmor, "minArmor");
            this.maxArmor = Objects.requireNonNull(maxArmor, "maxArmor");
            return this;
        }

        public Builder armorRange(SdkBotArmor minArmor, SdkBotArmor maxArmor) {
            this.minArmor = Objects.requireNonNull(minArmor, "minArmor").apiValue();
            this.maxArmor = Objects.requireNonNull(maxArmor, "maxArmor").apiValue();
            return this;
        }

        public Builder totemCount(int totemCount) {
            this.totemCount = totemCount;
            return this;
        }

        public Builder totemRange(int minTotemCount, int maxTotemCount) {
            this.minTotemCount = minTotemCount;
            this.maxTotemCount = maxTotemCount;
            return this;
        }

        public Builder rank(String rank) {
            this.rank = Objects.requireNonNull(rank, "rank");
            return this;
        }

        public Builder rank(SdkBotRank rank) {
            this.rank = Objects.requireNonNull(rank, "rank").apiValue();
            return this;
        }

        public Builder rankRange(String minRank, String maxRank) {
            this.minRank = Objects.requireNonNull(minRank, "minRank");
            this.maxRank = Objects.requireNonNull(maxRank, "maxRank");
            return this;
        }

        public Builder rankRange(SdkBotRank minRank, SdkBotRank maxRank) {
            this.minRank = Objects.requireNonNull(minRank, "minRank").apiValue();
            this.maxRank = Objects.requireNonNull(maxRank, "maxRank").apiValue();
            return this;
        }

        public Builder spawnLocation(@Nullable BotLocationRequest spawnLocation) {
            this.spawnLocation = spawnLocation;
            return this;
        }

        public Builder autoTarget(boolean autoTarget) {
            this.autoTarget = autoTarget;
            return this;
        }

        public Builder autoTargetRange(double autoTargetRange) {
            this.autoTargetRange = autoTargetRange;
            return this;
        }

        public Builder attackBots(boolean attackBots) {
            this.attackBots = attackBots;
            return this;
        }

        public Builder targetMode(SdkBotTargetMode targetMode) {
            this.targetMode = Objects.requireNonNull(targetMode, "targetMode");
            return this;
        }

        public Builder respectWorldGuardPvp(boolean respectWorldGuardPvp) {
            this.respectWorldGuardPvp = respectWorldGuardPvp;
            return this;
        }

        public Builder stayAfterOwnerDeath(boolean stayAfterOwnerDeath) {
            this.stayAfterOwnerDeath = stayAfterOwnerDeath;
            return this;
        }

        public Builder idleWander(boolean idleWander) {
            this.idleWander = idleWander;
            return this;
        }

        public Builder idleWanderRadius(double idleWanderRadius) {
            this.idleWanderRadius = idleWanderRadius;
            return this;
        }

        public Builder idleReturnDistance(double idleReturnDistance) {
            this.idleReturnDistance = idleReturnDistance;
            return this;
        }

        public Builder idleReturnDelayMs(long idleReturnDelayMs) {
            this.idleReturnDelayMs = idleReturnDelayMs;
            return this;
        }

        public Builder crystalPvp(boolean crystalPvp) {
            this.crystalPvp = crystalPvp;
            return this;
        }

        public Builder explosions(boolean explosions) {
            this.explosions = explosions;
            return this;
        }

        public Builder explosionBlockDamage(boolean explosionBlockDamage) {
            this.explosionBlockDamage = explosionBlockDamage;
            return this;
        }

        public Builder enderPearls(boolean enderPearls) {
            this.enderPearls = enderPearls;
            return this;
        }

        public Builder healing(boolean healing) {
            this.healing = healing;
            return this;
        }

        public Builder disableHealing() {
            this.healing = false;
            return this;
        }

        public Builder killMessage(String killMessage) {
            this.killMessageEnabled = true;
            this.killMessage = Objects.requireNonNull(killMessage, "killMessage");
            return this;
        }

        public Builder disableKillMessage() {
            this.killMessageEnabled = false;
            this.killMessage = null;
            return this;
        }

        public Builder equipmentSlots(@Nullable Map<SdkBotEquipmentSlot, BotEquipmentSlotRequest> equipmentSlots) {
            this.equipmentSlots = equipmentSlots == null ? Map.of() : Map.copyOf(equipmentSlots);
            return this;
        }

        public Builder equipmentSlot(SdkBotEquipmentSlot slot, BotEquipmentSlotRequest setting) {
            java.util.EnumMap<SdkBotEquipmentSlot, BotEquipmentSlotRequest> updated =
                    new java.util.EnumMap<>(SdkBotEquipmentSlot.class);
            updated.putAll(equipmentSlots);
            BotEquipmentSlotRequest requiredSetting = Objects.requireNonNull(setting, "setting");
            if (requiredSetting.mode() == SdkBotEquipmentSlotMode.DEFAULT) {
                updated.remove(Objects.requireNonNull(slot, "slot"));
            } else {
                updated.put(Objects.requireNonNull(slot, "slot"), requiredSetting);
            }
            this.equipmentSlots = Map.copyOf(updated);
            return this;
        }

        public Builder emptyEquipmentSlot(SdkBotEquipmentSlot slot) {
            return equipmentSlot(slot, BotEquipmentSlotRequest.empty());
        }

        public Builder equipmentItem(SdkBotEquipmentSlot slot, String material, int amount) {
            return equipmentSlot(slot, BotEquipmentSlotRequest.item(material, amount));
        }

        public Builder defaultEquipmentSlot(SdkBotEquipmentSlot slot) {
            return equipmentSlot(slot, BotEquipmentSlotRequest.defaultSlot());
        }

        public Builder disableExplosiveCombat() {
            this.crystalPvp = false;
            this.explosions = false;
            return this;
        }

        public Builder stationary() {
            this.idleWander = false;
            return this;
        }

        public Builder wander(double radius, double returnDistance, long returnDelayMs) {
            this.idleWander = true;
            this.idleWanderRadius = radius;
            this.idleReturnDistance = returnDistance;
            this.idleReturnDelayMs = returnDelayMs;
            return this;
        }

        public EventBotSpawnRequest build() {
            return new EventBotSpawnRequest(
                    ownerUUID,
                    botUUID,
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
                    attackBots,
                    targetMode,
                    respectWorldGuardPvp,
                    stayAfterOwnerDeath,
                    idleWander,
                    idleWanderRadius,
                    idleReturnDistance,
                    idleReturnDelayMs,
                    crystalPvp,
                    explosions,
                    explosionBlockDamage,
                    enderPearls,
                    healing,
                    killMessageEnabled,
                    killMessage,
                    equipmentSlots);
        }
    }
}
